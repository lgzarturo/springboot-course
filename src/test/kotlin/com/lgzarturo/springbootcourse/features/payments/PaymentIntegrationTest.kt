package com.lgzarturo.springbootcourse.features.payments

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.payments.dto.CreatePaymentRequest
import com.lgzarturo.springbootcourse.features.payments.dto.PaymentResponse
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import com.lgzarturo.springbootcourse.features.reservations.dto.CreateReservationRequest
import com.lgzarturo.springbootcourse.features.reservations.dto.ReservationResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.UpdateReservationStatusRequest
import com.lgzarturo.springbootcourse.features.rooms.PokemonTheme
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import com.lgzarturo.springbootcourse.features.rooms.dto.CreateRoomRequest
import com.lgzarturo.springbootcourse.features.rooms.dto.RoomResponse
import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import com.lgzarturo.springbootcourse.features.users.CreateUserCommand
import com.lgzarturo.springbootcourse.features.users.CreateUserService
import com.lgzarturo.springbootcourse.features.users.UserRole
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
 * Pruebas de integración del flujo completo de pagos.
 * Cubre creación, importe, transición de reserva, duplicados, fallos y reembolsos.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "springdoc.api-docs.path=/api-docs",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Payment Integration Tests")
class PaymentIntegrationTest {
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
        val email = "pay.it.${System.nanoTime()}@pokemon.com"
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
        val email = "pay.it.${System.nanoTime()}@pokemon.com"
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
                    CreateHotelRequest("Hotel Pagos E2E", "Calle E2E 1"),
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
        checkIn: LocalDate = LocalDate.of(2026, 11, 1),
        checkOut: LocalDate = LocalDate.of(2026, 11, 4),
    ) = CreateReservationRequest(
        roomId = roomId,
        userId = userId,
        guestName = "Ash Ketchum",
        checkIn = checkIn,
        checkOut = checkOut,
    )

    private fun createReservation(
        roomId: String,
        userId: Long,
        checkIn: LocalDate = LocalDate.of(2026, 11, 1),
        checkOut: LocalDate = LocalDate.of(2026, 11, 4),
    ): String {
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                createRequest(roomId, userId, checkIn, checkOut),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, created.statusCode)
        return created.body?.id ?: error("Reserva sin id")
    }

    private fun jsonEntity(body: Any): HttpEntity<Any> =
        HttpEntity(body, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })

    private fun confirmReservation(reservationId: String) {
        val confirmed =
            testRestTemplate.exchange(
                "/api/v1/reservations/$reservationId/status",
                HttpMethod.PATCH,
                jsonEntity(UpdateReservationStatusRequest(status = ReservationStatus.CONFIRMED)),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.OK, confirmed.statusCode)
    }

    private fun reservationStatus(reservationId: String): ReservationStatus {
        val detail =
            testRestTemplate.getForEntity("/api/v1/reservations/$reservationId", ReservationResponse::class.java)
        assertEquals(HttpStatus.OK, detail.statusCode)
        return detail.body?.status ?: error("Reserva sin estado")
    }

    @Test
    @DisplayName("Flujo completo: pagar mueve a IN_PROGRESS y el reembolso cierra el pago")
    fun `should pay move reservation to in progress and refund the payment`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.pay@pokemon.com")
        val reservationId = createReservation(roomId, userId)
        confirmReservation(reservationId)

        // When: pagar (3 noches x 150.0)
        val paid =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = reservationId),
                PaymentResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, paid.statusCode)
        assertEquals(PaymentStatus.COMPLETED, paid.body?.status)
        assertEquals(450.0, paid.body?.amount)
        assertEquals(reservationId, paid.body?.reservationId)
        assertNotNull(paid.body?.id)
        assertTrue(paid.body?.gatewayReference?.isNotBlank() == true)
        val paymentId = paid.body?.id ?: error("Pago sin id")

        // When: la reserva avanza a IN_PROGRESS
        assertEquals(ReservationStatus.IN_PROGRESS, reservationStatus(reservationId))

        // When: detalle del pago
        val detail = testRestTemplate.getForEntity("/api/v1/payments/$paymentId", PaymentResponse::class.java)
        assertEquals(HttpStatus.OK, detail.statusCode)
        assertEquals(PaymentStatus.COMPLETED, detail.body?.status)

        // When: reembolsar
        val refunded =
            testRestTemplate.postForEntity(
                "/api/v1/payments/$paymentId/refund",
                null,
                PaymentResponse::class.java,
            )
        assertEquals(HttpStatus.OK, refunded.statusCode)
        assertEquals(PaymentStatus.REFUNDED, refunded.body?.status)
    }

    @Test
    @DisplayName("POST debe rechazar reservas no confirmadas, inexistentes o en blanco")
    fun `should reject payments for pending missing or blank reservations`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.payfail@pokemon.com")
        val pendingId = createReservation(roomId, userId)

        // When: pagar una reserva PENDING -> 409
        val pending =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = pendingId),
                String::class.java,
            )
        assertEquals(HttpStatus.CONFLICT, pending.statusCode)

        // When: reserva inexistente -> 404
        val missing =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = "reserva-inexistente"),
                String::class.java,
            )
        assertEquals(HttpStatus.NOT_FOUND, missing.statusCode)

        // When: reserva en blanco -> 400
        val blank =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = " "),
                String::class.java,
            )
        assertEquals(HttpStatus.BAD_REQUEST, blank.statusCode)
    }

    @Test
    @DisplayName("POST debe rechazar duplicados pero permitir reintento tras FAILED")
    fun `should reject duplicates but allow retry after failure`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.paydup@pokemon.com")
        val firstId = createReservation(roomId, userId)
        confirmReservation(firstId)
        val secondId =
            createReservation(roomId, userId, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3))
        confirmReservation(secondId)

        // When: pagar la primera -> 201
        val first =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = firstId),
                PaymentResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, first.statusCode)

        // When: duplicado -> 409
        val duplicate =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = firstId),
                String::class.java,
            )
        assertEquals(HttpStatus.CONFLICT, duplicate.statusCode)

        // When: pago fallido simulado -> 201 FAILED y la reserva sigue CONFIRMED
        val failed =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = secondId, simulateFailure = true),
                PaymentResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, failed.statusCode)
        assertEquals(PaymentStatus.FAILED, failed.body?.status)
        assertEquals(300.0, failed.body?.amount)
        assertEquals(ReservationStatus.CONFIRMED, reservationStatus(secondId))
        val failedId = failed.body?.id ?: error("Pago sin id")

        // When: reembolsar un FAILED -> 409
        val badRefund =
            testRestTemplate.postForEntity("/api/v1/payments/$failedId/refund", null, String::class.java)
        assertEquals(HttpStatus.CONFLICT, badRefund.statusCode)

        // When: reintento -> 201 COMPLETED
        val retry =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = secondId),
                PaymentResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, retry.statusCode)
        assertEquals(PaymentStatus.COMPLETED, retry.body?.status)
        assertEquals(ReservationStatus.IN_PROGRESS, reservationStatus(secondId))
    }

    @Test
    @DisplayName("GET listado debe filtrar por reserva y estado, y 404 en detalle inexistente")
    fun `should list payments filtered and return 404 for missing payments`() {
        // Given
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser("ash.paylist@pokemon.com")
        val reservationId = createReservation(roomId, userId)
        confirmReservation(reservationId)
        val paid =
            testRestTemplate.postForEntity(
                "/api/v1/payments",
                CreatePaymentRequest(reservationId = reservationId),
                PaymentResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, paid.statusCode)

        // When: filtro por reserva
        val byReservation =
            testRestTemplate.getForEntity(
                "/api/v1/payments?reservationId=$reservationId&page=0&size=10",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, byReservation.statusCode)
        assertTrue(byReservation.body?.contains("COMPLETED") == true)

        // When: filtro por estado
        val byStatus =
            testRestTemplate.getForEntity(
                "/api/v1/payments?status=COMPLETED&page=0&size=10",
                String::class.java,
            )
        assertEquals(HttpStatus.OK, byStatus.statusCode)
        assertTrue(byStatus.body?.contains(reservationId) == true)

        // When: detalle inexistente -> 404
        val missing = testRestTemplate.getForEntity("/api/v1/payments/pago-inexistente", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, missing.statusCode)

        // When: reembolso inexistente -> 404
        val missingRefund =
            testRestTemplate.postForEntity("/api/v1/payments/pago-inexistente/refund", null, String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, missingRefund.statusCode)
    }
}
