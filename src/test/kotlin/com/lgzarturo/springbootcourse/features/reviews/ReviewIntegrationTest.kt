package com.lgzarturo.springbootcourse.features.reviews

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import com.lgzarturo.springbootcourse.features.reservations.dto.CreateReservationRequest
import com.lgzarturo.springbootcourse.features.reservations.dto.ReservationResponse
import com.lgzarturo.springbootcourse.features.reservations.dto.UpdateReservationStatusRequest
import com.lgzarturo.springbootcourse.features.reviews.dto.CreateReviewRequest
import com.lgzarturo.springbootcourse.features.reviews.dto.ReviewResponse
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
 * Pruebas de integración del flujo completo de reseñas.
 * Cubre autorización por propietario, unicidad, validación, paginación y promedio.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "springdoc.api-docs.path=/api-docs",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Review Integration Tests")
class ReviewIntegrationTest {
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
                request.headers.setBearerAuth(defaultGuestToken)
                execution.execute(request, body)
            },
        )
        template
    }

    private val defaultGuestToken: String by lazy {
        val email = "rev.it.${System.nanoTime()}@pokemon.com"
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

    private val staffTokenValue: String by lazy {
        val email = "rev.it.${System.nanoTime()}@pokemon.com"
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

    private val tokensByEmail = mutableMapOf<String, String>()

    private fun tokenFor(email: String): String =
        tokensByEmail.getOrPut(email) { authService.authenticate(email, "Pikachu123") }

    private fun staffToken(): String = staffTokenValue

    private fun <T> withToken(
        token: String,
        block: () -> T,
    ): T {
        val template = testRestTemplate.restTemplate
        val original = template.interceptors.toList()
        template.interceptors.clear()
        template.interceptors.add(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(token)
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
            withToken(staffToken()) {
                testRestTemplate.postForEntity(
                    "/api/v1/hotels",
                    CreateHotelRequest("Hotel Reseñas E2E", "Calle E2E 1"),
                    HotelResponse::class.java,
                )
            }
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body?.id ?: error("Hotel sin id")
    }

    private fun createRoom(
        hotelId: String,
        number: String = "101",
    ): String {
        val request =
            CreateRoomRequest(
                number = number,
                type = "SUITE",
                status = RoomStatus.AVAILABLE,
                theme = PokemonTheme.PIKACHU,
                capacity = 2,
                price = 150.0,
                description = "Suite con vista al lago",
            )
        val response =
            withToken(staffToken()) {
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

    private fun createReservation(
        roomId: String,
        userId: Long,
        checkIn: LocalDate = LocalDate.of(2026, 11, 1),
        checkOut: LocalDate = LocalDate.of(2026, 11, 4),
    ): String {
        val created =
            testRestTemplate.postForEntity(
                "/api/v1/reservations",
                CreateReservationRequest(
                    roomId = roomId,
                    userId = userId,
                    guestName = "Ash Ketchum",
                    checkIn = checkIn,
                    checkOut = checkOut,
                ),
                ReservationResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, created.statusCode)
        return created.body?.id ?: error("Reserva sin id")
    }

    private fun completeReservation(reservationId: String) {
        listOf(
            ReservationStatus.CONFIRMED,
            ReservationStatus.IN_PROGRESS,
            ReservationStatus.COMPLETED,
        ).forEach { status ->
            val updated =
                testRestTemplate.exchange(
                    "/api/v1/reservations/$reservationId/status",
                    HttpMethod.PATCH,
                    HttpEntity(
                        UpdateReservationStatusRequest(status = status),
                        HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON },
                    ),
                    ReservationResponse::class.java,
                )
            assertEquals(HttpStatus.OK, updated.statusCode)
        }
    }

    @Test
    @DisplayName("Flujo completo: el propietario opina y el hotel resume sus reseñas")
    fun `should create review and summarize hotel reviews`() {
        // Given
        val email = "ash.review@pokemon.com"
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser(email)
        val reservationId = createReservation(roomId, userId)
        completeReservation(reservationId)

        // When: el propietario crea su reseña
        val created =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = reservationId, rating = 5, comment = "Genial"),
                    ReviewResponse::class.java,
                )
            }

        // Then
        assertEquals(HttpStatus.CREATED, created.statusCode)
        assertEquals(reservationId, created.body?.reservationId)
        assertEquals(hotelId, created.body?.hotelId)
        assertEquals(userId, created.body?.userId)
        assertEquals(5, created.body?.rating)
        val reviewId = created.body?.id ?: error("Reseña sin id")
        assertNotNull(reviewId)

        // When: detalle de la reseña
        val detail =
            withToken(tokenFor(email)) {
                testRestTemplate.getForEntity("/api/v1/reviews/$reviewId", ReviewResponse::class.java)
            }
        assertEquals(HttpStatus.OK, detail.statusCode)
        assertEquals(5, detail.body?.rating)

        // When: resumen paginado del hotel
        val summary =
            withToken(tokenFor(email)) {
                testRestTemplate.getForEntity(
                    "/api/v1/hotels/$hotelId/reviews?page=0&size=10",
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.OK, summary.statusCode)
        assertTrue(summary.body?.contains(reviewId) == true)
        assertTrue(summary.body?.contains("averageRating") == true)
    }

    @Test
    @DisplayName("POST debe rechazar no completadas, ajenas y duplicadas")
    fun `should reject pending foreign and duplicate reviews`() {
        // Given
        val email = "ash.reviewfail@pokemon.com"
        val otherEmail = "misty.reviewfail@pokemon.com"
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser(email)
        createUser(otherEmail)
        val pendingId = createReservation(roomId, userId)
        val completedId =
            createReservation(roomId, userId, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3))
        completeReservation(completedId)

        // When: reserva PENDING -> 409
        val pending =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = pendingId, rating = 5),
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.CONFLICT, pending.statusCode)

        // When: otro usuario -> 403
        val foreign =
            withToken(tokenFor(otherEmail)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = completedId, rating = 5),
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.FORBIDDEN, foreign.statusCode)

        // When: primera reseña -> 201 y duplicada -> 409
        val first =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = completedId, rating = 4),
                    ReviewResponse::class.java,
                )
            }
        assertEquals(HttpStatus.CREATED, first.statusCode)
        val duplicate =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = completedId, rating = 5),
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.CONFLICT, duplicate.statusCode)
    }

    @Test
    @DisplayName("POST y GET deben rechazar valoraciones y detalles inválidos")
    fun `should reject invalid ratings and missing ids`() {
        // Given
        val email = "ash.reviewbad@pokemon.com"
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val userId = createUser(email)
        val completedId = createReservation(roomId, userId)
        completeReservation(completedId)

        // When: rating 6 -> 400 y reserva inexistente -> 404
        val invalid =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = completedId, rating = 6),
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.BAD_REQUEST, invalid.statusCode)
        val missing =
            withToken(tokenFor(email)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = "reserva-inexistente", rating = 5),
                    String::class.java,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, missing.statusCode)

        // When: detalle inexistente -> 404
        val missingDetail =
            withToken(tokenFor(email)) {
                testRestTemplate.getForEntity("/api/v1/reviews/resena-inexistente", String::class.java)
            }
        assertEquals(HttpStatus.NOT_FOUND, missingDetail.statusCode)
    }

    @Test
    @DisplayName("GET hotel debe paginar y promediar varias reseñas")
    fun `should paginate and average multiple hotel reviews`() {
        // Given: dos reservas completadas de distintos usuarios en el mismo hotel
        val hotelId = createHotel()
        val roomId = createRoom(hotelId)
        val firstEmail = "ash.avg@pokemon.com"
        val secondEmail = "misty.avg@pokemon.com"
        val firstUser = createUser(firstEmail)
        val secondUser = createUser(secondEmail)
        val firstReservation = createReservation(roomId, firstUser)
        val secondReservation =
            createReservation(roomId, secondUser, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 3))
        completeReservation(firstReservation)
        completeReservation(secondReservation)
        val first =
            withToken(tokenFor(firstEmail)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = firstReservation, rating = 5),
                    ReviewResponse::class.java,
                )
            }
        assertEquals(HttpStatus.CREATED, first.statusCode)
        val second =
            withToken(tokenFor(secondEmail)) {
                testRestTemplate.postForEntity(
                    "/api/v1/reviews",
                    CreateReviewRequest(reservationId = secondReservation, rating = 3),
                    ReviewResponse::class.java,
                )
            }
        assertEquals(HttpStatus.CREATED, second.statusCode)

        // When: primera página de tamaño 1
        val page =
            withToken(tokenFor(firstEmail)) {
                testRestTemplate.getForEntity(
                    "/api/v1/hotels/$hotelId/reviews?page=0&size=1",
                    String::class.java,
                )
            }

        // Then: promedio global (5+3)/2 sobre la página parcial
        assertEquals(HttpStatus.OK, page.statusCode)
        assertTrue(page.body?.contains("4.0") == true)
        assertTrue(page.body?.contains("totalElements") == true)
    }
}
