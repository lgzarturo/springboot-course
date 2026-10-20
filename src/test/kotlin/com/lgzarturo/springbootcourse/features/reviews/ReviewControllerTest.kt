package com.lgzarturo.springbootcourse.features.reviews

import com.lgzarturo.springbootcourse.features.reviews.dto.CreateReviewRequest
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@TestConfiguration
class ReviewMockkTestConfig {
    @Bean
    fun reviewService(): ReviewService = mockk()
}

/**
 * Tests del controlador REST de reseñas.
 * Verifica creación autorizada, validación, detalle y resumen paginado del hotel.
 */
@WebMvcTest(ReviewController::class)
@Import(ReviewMockkTestConfig::class, SecurityTestConfig::class)
@DisplayName("ReviewController Tests")
class ReviewControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = "/api/v1/reviews"

    private fun createRequest() = CreateReviewRequest(reservationId = "res-1", rating = 5, comment = "Genial")

    private fun sampleReview() =
        Review(
            id = "rev-1",
            reservationId = "res-1",
            hotelId = "hotel-1",
            userId = 1L,
            rating = 5,
            comment = "Genial",
        )

    @BeforeEach
    fun setUp() {
        clearMocks(reviewService)
    }

    @Test
    @WithMockUser(username = "ash@pokemon.com")
    @DisplayName("POST debe crear la reseña con la identidad autenticada y devolver 201")
    fun `should create review with authenticated identity and return 201`() {
        // Given
        every { reviewService.createReview(any(), any(), any(), any()) } returns sampleReview()

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", `is`("rev-1")))
            .andExpect(jsonPath("$.reservationId", `is`("res-1")))
            .andExpect(jsonPath("$.rating", `is`(5)))
        verify { reviewService.createReview("res-1", 5, "Genial", "ash@pokemon.com") }
    }

    @Test
    @WithMockUser(username = "ash@pokemon.com")
    @DisplayName("POST debe devolver 400 con valoraciones fuera de 1-5")
    fun `should return 400 with out-of-range ratings`() {
        // Given / When / Then: rating 6
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest().copy(rating = 6))),
            ).andExpect(status().isBadRequest)

        // Given / When / Then: rating 0
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest().copy(rating = 0))),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = "ash@pokemon.com")
    @DisplayName("POST debe devolver 403 sin propiedad, 409 sin estado y 404 sin reserva")
    fun `should return 403 409 and 404 for forbidden conflict and missing`() {
        // Given: no propietario
        every { reviewService.createReview(any(), any(), any(), any()) } throws
            AccessDeniedException("Solo el propietario puede opinar")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isForbidden)

        // Given: reserva no completada o duplicada
        every { reviewService.createReview(any(), any(), any(), any()) } throws
            IllegalStateException("La reserva no está completada")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isConflict)

        // Given: reserva inexistente
        every { reviewService.createReview(any(), any(), any(), any()) } throws
            NoSuchElementException("Reserva no encontrada: missing")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET debe devolver el detalle o 404 si no existe")
    fun `should return detail or 404 when missing`() {
        // Given
        every { reviewService.getReviewById("rev-1") } returns sampleReview()
        every { reviewService.getReviewById("missing") } returns null

        // When / Then
        mockMvc
            .perform(get("$baseUrl/rev-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", `is`("rev-1")))
            .andExpect(jsonPath("$.rating", `is`(5)))

        mockMvc
            .perform(get("$baseUrl/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET hotel debe paginar reseñas con el promedio")
    fun `should list hotel reviews paginated with average`() {
        // Given
        every { reviewService.getHotelReviews(any(), any(), any()) } returns
            Triple(listOf(sampleReview()), 1L, 5.0)

        // When / Then
        mockMvc
            .perform(get("/api/v1/hotels/hotel-1/reviews?page=0&size=10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.totalElements", `is`(1)))
            .andExpect(jsonPath("$.averageRating", `is`(5.0)))
    }
}
