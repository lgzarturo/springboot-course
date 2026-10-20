package com.lgzarturo.springbootcourse.features.reviews

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests unitarios del modelo de dominio Review.
 * Verifica el invariante de valoración 1-5 y el cálculo del promedio sin Spring.
 */
@DisplayName("Review Domain Tests")
class ReviewTest {
    private fun sampleReview(rating: Int = 5) =
        Review(
            id = "rev-1",
            reservationId = "res-1",
            hotelId = "hotel-1",
            userId = 1L,
            rating = rating,
            comment = "Estancia excelente",
        )

    @Test
    @DisplayName("Debería aceptar valoraciones en el rango 1-5")
    fun `should accept ratings within range`() {
        // Given / When / Then: límites incluidos
        assertEquals(1, sampleReview(rating = 1).rating)
        assertEquals(5, sampleReview(rating = 5).rating)
        assertEquals(3, sampleReview(rating = 3).rating)
    }

    @Test
    @DisplayName("Debería rechazar valoraciones fuera del rango 1-5")
    fun `should reject ratings outside range`() {
        // Given / When / Then
        assertThrows<IllegalArgumentException> { sampleReview(rating = 0) }
        assertThrows<IllegalArgumentException> { sampleReview(rating = 6) }
        assertThrows<IllegalArgumentException> { sampleReview(rating = -1) }
    }

    @Test
    @DisplayName("Debería rechazar una reserva vacía")
    fun `should reject blank reservation id`() {
        // Given / When / Then
        assertThrows<IllegalArgumentException> {
            sampleReview().copy(reservationId = "  ")
        }
    }

    @Test
    @DisplayName("Debería calcular el promedio y devolver 0.0 sin valoraciones")
    fun `should calculate average rating`() {
        // Given / When / Then
        assertEquals(4.0, Review.averageRating(listOf(5, 3)))
        assertEquals(5.0, Review.averageRating(listOf(5)))
        assertEquals(0.0, Review.averageRating(emptyList()))
    }
}
