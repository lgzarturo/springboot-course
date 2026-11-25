package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.CreateReservationRequest
import com.lgzarturo.springbootcourse.features.reservations.dto.ReservationResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.UpdateReservationStatusRequest
import com.lgzarturo.springbootcourse.features.rooms.PokemonTheme
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import com.lgzarturo.springbootcourse.features.rooms.dto.CreateRoomRequest
import com.lgzarturo.springbootcourse.features.rooms.dto.RoomResponse
import com.lgzarturo.springbootcourse.features.users.CreateUserCommand
import com.lgzarturo.springbootcourse.features.users.CreateUserService
import com.lgzarturo.springbootcourse.features.users.UserRole
import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.DefaultUriBuilderFactory
import java.time.LocalDate

/**
 * Pruebas de integración del flujo completo de reservas.
 * Cubre creación, solape, precio, transiciones, sincronización de habitación y filtros.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "springdoc.api-docs.path=/api-docs",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Reservation Integration Tests")
class ReservationIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Autowired
    private lateinit var authService: AuthService

    private val testRestTemplate: TestRestTemplate by lazy {
        val template = TestRestTemplate()
        val factory = DefaultUriBuilderFactory("http://localhost:$port")
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        template.restTemplate.uriTemplateHandler = factory
        template.restTemplate.interceptors.add(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(guestToken())
                execution.execute(request, body)
            },
        )
        template
    }

    private val guestTokenValue: String by lazy {
        val email = "res.it.${System.nanoTime()}@pokemon.com"
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Guest",
                lastName = "User",
                phoneNumber = null,
                role = UserRole.GUEST,
            ),
        )
        authService.authenticate(email, "Password123!")
    }

    private fun guestToken(): String = guestTokenValue

    private val staffTokenValue: String by lazy {
        val email = "res.it.${System.nanoTime()}@pokemon.com"
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Staff",
                lastName = "Member",
                phoneNumber = null,
                role = UserRole.STAFF,
            ),
        )
        authService.authenticate(email, "Password123!")
    }

    private fun staffToken(): String = staffTokenValue

    private fun <T> withStaffToken(block: () -> T): T {
        val template = testRestTemplate.restTemplate
        val original = template.interceptors.toList()
        template.interceptors.clear()
        template.interceptors.add(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(staffToken())
                execution.execute(request, body)
            },
        )
        try {
            return block()
        } finally {
            template.interceptors.clear()
            template.interceptors.addAll(original)
        }
    }

    private fun createHotel(): String {
        val response =
            withStaffToken {
                testRestTemplate.postForEntity(
                    "/api/v1/hotels",
                    CreateHotelRequest("Hotel Reservas E2E", "Calle E2E 1"),
                    HotelResponse::class.java,
                )
            }
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body?.id ?: error("Hotel sin id")
    }

    private fun createRoom(hotelId: String): String {
        val request =
            CreateRoomRequest(
                number = "101",
                type = "SUITE",
                status = RoomStatus.AVAILABLE,
                theme = PokemonTheme.PIKACHU,
                capacity = 2,
                price = 150.0,
                description = "Suite con vista al lago",
            )
        val response =
            withStaffToken {
                testRestTemplate.postForEntity("/api/v1/hotels/$hotelId/rooms", request, RoomResponse::class.java)
            }
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body?.id ?: error("Habitación sin id")
    }

    private fun createUser(email: String): Long {
        val response =
            testRestTemplate.postForEntity(
                "/api/v1/users",
                CreateUserRequest(
                    email = email,
                    password = "Pikachu123",
                    firstName = "Ash",
                    lastName = "Ketchum",
                ),
                UserResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body?.id ?: error("Usuario sin id")
    }

    private fun createRequest(
        roomId: String,
        userId: Long,
    ) = CreateReservationRequest(
        roomId = roomId,
        userId = userId,
        guestName = "Ash Ketchum",
        checkIn = LocalDate.of(2026, 11, 1),
        checkOut = LocalDate.of(2026, 11, 4),
    )

    private fun jsonEntity(body: Any): HttpEntity<Any> =
        HttpEntity(body, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })

    private fun changeStatus(
        reservationId: String,
        status: ReservationStatus,
    ) = testRestTemplate.exchange(
        "/api/v1/reservations/$reservationId/status",
        HttpMethod.PATCH,
        jsonEntity(UpdateReservationStatusRequest(status = status)),
        ReservationResponse::class.java,
    )

    private fun changeStatusRaw(
        reservationId: String,
        status: ReservationStatus,
    ) = testRestTemplate.exchange(
        "/api/v1/reservations/$reservationId/status",
        HttpMethod.PATCH,
        jsonEntity(UpdateReservationStatusRequest(status = status)),
        String::class.java,
    )

    private fun roomStatus(
        hotelId: String,
        roomId: String,
    ): RoomStatus {
        val response =
            testRestTemplate.getForEntity("/api/v1/hotels/$hotelId/rooms/$roomId", RoomResponse::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        return response.body?.status ?: error("Habitación sin estado")
    }

    @Test
    @DisplayName("Flujo completo: crear, confirmar, check-in y check-out con precio calculado")
    fun `should complete the full booking flow with computed price`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.flow@pokemon.com")

        // When: crear reserva (3 noches x 150.0)
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId),
                ReservationResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, created.statusCode)
        assertNotNull(created.body?.id)
        assertEquals(450.0, created.body?.totalPrice)
        assertEquals(ReservationStatus.PENDING, created.body?.status)
        val reservationId = created.body?.id ?: error("Reserva sin id")

        // When: confirmar -> habitación RESERVED
        val confirmed = changeStatus(reservationId, ReservationStatus.CONFIRMED)
        assertEquals(HttpStatus.OK, confirmed.statusCode)
        assertEquals(ReservationStatus.CONFIRMED, confirmed.body?.status)
        assertEquals(RoomStatus.RESERVED, roomStatus(hotelId, roomId))

        // When: check-in -> IN_PROGRESS, check-out -> COMPLETED
        assertEquals(
            ReservationStatus.IN_PROGRESS,
            changeStatus(reservationId, ReservationStatus.IN_PROGRESS).body?.status,
        )
        assertEquals(ReservationStatus.COMPLETED, changeStatus(reservationId, ReservationStatus.COMPLETED).body?.status)

        // When: detalle final
        val detail =
            testRestTemplate.getForEntity("/api/v1/reservations/$reservationId", ReservationResponse::class.java)
        assertEquals(HttpStatus.OK, detail.statusCode)
        assertEquals(ReservationStatus.COMPLETED, detail.body?.status)
    }

    @Test
    @DisplayName("POST debe rechazar solapes, rangos inválidos y reservas adyacentes sí pasan")
    fun `should reject overlaps and invalid ranges but allow adjacent bookings`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.overlap@pokemon.com")
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, created.statusCode)

        // When: solape interior -> 409
        val overlap =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId).copy(
                    checkIn = LocalDate.of(2026, 11, 2),
                    checkOut = LocalDate.of(2026, 11, 5),
                ),
                String::class.java,
            )
        assertEquals(HttpStatus.CONFLICT, overlap.statusCode)

        // When: rango invertido -> 400
        val invalid =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId).copy(
                    checkIn = LocalDate.of(2026, 11, 4),
                    checkOut = LocalDate.of(2026, 11, 1),
                ),
                String::class.java,
            )
        assertEquals(HttpStatus.BAD_REQUEST, invalid.statusCode)

        // When: reserva adyacente exacta -> 201
        val adjacent =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId).copy(
                    checkIn = LocalDate.of(2026, 11, 4),
                    checkOut = LocalDate.of(2026, 11, 6),
                ),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, adjacent.statusCode)
        assertEquals(300.0, adjacent.body?.totalPrice)
    }

    @Test
    @DisplayName("Cancelar debe restaurar la habitación a AVAILABLE y liberar las fechas")
    fun `should restore room to available and free dates on cancel`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.cancel@pokemon.com")
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId),
                ReservationResponse::class.java,
            )
        val reservationId = created.body?.id ?: error("Reserva sin id")
        changeStatus(reservationId, ReservationStatus.CONFIRMED)
        assertEquals(RoomStatus.RESERVED, roomStatus(hotelId, roomId))

        // When: cancelar
        val cancelled = changeStatus(reservationId, ReservationStatus.CANCELLED)

        // Then
        assertEquals(HttpStatus.OK, cancelled.statusCode)
        assertEquals(RoomStatus.AVAILABLE, roomStatus(hotelId, roomId))

        // When: las fechas liberadas aceptan una nueva reserva
        val rebooked =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, rebooked.statusCode)
    }

    @Test
    @DisplayName("PATCH debe rechazar transiciones inválidas y reservas inexistentes")
    fun `should reject invalid transitions and missing reservations`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.invalid@pokemon.com")
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId),
                ReservationResponse::class.java,
            )
        val reservationId = created.body?.id ?: error("Reserva sin id")

        // When: salto PENDING -> COMPLETED -> 409
        val invalid = changeStatusRaw(reservationId, ReservationStatus.COMPLETED)
        assertEquals(HttpStatus.CONFLICT, invalid.statusCode)

        // When: reserva inexistente -> 404
        val missing = changeStatusRaw("reserva-inexistente", ReservationStatus.CONFIRMED)
        assertEquals(HttpStatus.NOT_FOUND, missing.statusCode)

        // When: detalle inexistente -> 404
        val detail = testRestTemplate.getForEntity("/api/v1/reservations/reserva-inexistente", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, detail.statusCode)
    }

    @Test
    @DisplayName("GET listado debe filtrar por usuario, estado y fechas con paginación")
    fun `should list reservations filtered by user status and dates`() {
        // Given: dos reservas en meses distintos
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val firstUserId = createUser("ash.filters@pokemon.com")
        val secondUserId = createUser("misty.filters@pokemon.com")
        testRestTemplate.postForEntity(
            "/api/v1/reservations",
            createRequest(roomId, firstUserId),
            ReservationResponse::class.java,
        )
        testRestTemplate.postForEntity(
            "/api/v1/reservations",
            createRequest(roomId, secondUserId).copy(
                guestName = "Misty",
                checkIn = LocalDate.of(2026, 12, 1),
                checkOut = LocalDate.of(2026, 12, 3),
            ),
            ReservationResponse::class.java,
        )

        // When: filtro por usuario
        val byUser =
            testRestTemplate.getForEntity(
                "/api/v1/reservations?userId=$secondUserId&page=0&size=10",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, byUser.statusCode)
        assertTrue(byUser.body?.contains("\"guestName\":\"Misty\"") == true)
        assertTrue(byUser.body?.contains("\"totalElements\":1") == true)

        // When: filtro por estado y rango de fechas
        val byStatusAndDates =
            testRestTemplate.getForEntity(
                "/api/v1/reservations?status=PENDING&from=2026-12-01&to=2026-12-31&page=0&size=10",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, byStatusAndDates.statusCode)
        assertTrue(byStatusAndDates.body?.contains("\"guestName\":\"Misty\"") == true)
        assertTrue(byStatusAndDates.body?.contains("\"totalElements\":1") == true)
    }

    @Test
    @DisplayName("OpenAPI debe documentar los endpoints de reservas")
    fun `should expose reservation endpoints in OpenAPI document`() {
        // When
        val response = testRestTemplate.getForEntity("/api-docs", String::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.contains("/api/v1/reservations") == true)
        assertTrue(response.body?.contains("/api/v1/reservations/{id}") == true)
    }
}
