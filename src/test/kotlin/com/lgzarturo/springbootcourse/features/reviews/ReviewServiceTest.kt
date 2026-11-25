package com.lgzarturo.springbootcourse.features.reviews

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.reservations.ReservationEntity
import com.lgzarturo.springbootcourse.features.reservations.ReservationJpaRepository
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDate
import java.util.Optional

/**
 * Tests unitarios para ReviewService.
 * Verifica autorización por propietario, unicidad, validación y promedios sin Spring.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("ReviewService Tests")
class ReviewServiceTest {
    @MockK
    private lateinit var reviewJpaRepository: ReviewJpaRepository

    @MockK
    private lateinit var reservationJpaRepository: ReservationJpaRepository

    private lateinit var reviewService: ReviewService

    private fun sampleReservationEntity(
        status: ReservationStatus = ReservationStatus.COMPLETED,
        userId: Long = 1L,
    ) = ReservationEntity(
        id = "res-1",
        room =
            RoomEntity(
                id = "room-1",
                number = "101",
                type = "SUITE",
                price = 150.0,
                hotel = HotelEntity(id = "hotel-1", name = "Hotel Pikachu", address = "Calle 1"),
            ),
        userId = userId,
        guestName = "Ash Ketchum",
        checkIn = LocalDate.of(2026, 11, 1),
        checkOut = LocalDate.of(2026, 11, 4),
        totalPrice = 450.0,
        status = status,
    )

    private fun sampleReviewEntity(id: String? = "rev-1") =
        ReviewEntity(
            id = id,
            reservationId = "res-1",
            hotelId = "hotel-1",
            userId = 1L,
            rating = 5,
            comment = "Estancia excelente",
        )

    @BeforeEach
    fun setUp() {
        reviewService = ReviewService(reviewJpaRepository, reservationJpaRepository)
    }

    @Test
    @DisplayName("Debería crear la reseña del propietario de una reserva completada")
    fun `createReview should create review for completed reservation owner`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { reviewJpaRepository.existsByReservationId("res-1") } returns false
        every { reviewJpaRepository.save(any()) } returns sampleReviewEntity()

        // When
        val result = reviewService.createReview("res-1", 5, "Estancia excelente", 1L)

        // Then
        assertEquals("rev-1", result.id)
        assertEquals("res-1", result.reservationId)
        assertEquals("hotel-1", result.hotelId)
        assertEquals(1L, result.userId)
        assertEquals(5, result.rating)
        verify {
            reviewJpaRepository.save(
                match<ReviewEntity> { it.rating == 5 && it.hotelId == "hotel-1" },
            )
        }
    }

    @Test
    @DisplayName("Debería rechazar reseñas de usuarios que no son propietarios")
    fun `createReview should reject non-owners`() {
        // Given: la reserva pertenece a otro usuario
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleReservationEntity(userId = 2L))

        // When / Then
        assertThrows<AccessDeniedException> { reviewService.createReview("res-1", 5, "", 1L) }
        verify(exactly = 0) { reviewJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar reservas no completadas")
    fun `createReview should reject non-completed reservations`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleReservationEntity(status = ReservationStatus.CONFIRMED))

        // When / Then
        assertThrows<IllegalStateException> { reviewService.createReview("res-1", 5, "", 1L) }

        // Given: reserva cancelada
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleReservationEntity(status = ReservationStatus.CANCELLED))

        // When / Then: sin persistir
        assertThrows<IllegalStateException> { reviewService.createReview("res-1", 5, "", 1L) }
        verify(exactly = 0) { reviewJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar una segunda reseña para la misma reserva")
    fun `createReview should reject duplicate reviews`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { reviewJpaRepository.existsByReservationId("res-1") } returns true

        // When / Then
        assertThrows<IllegalStateException> { reviewService.createReview("res-1", 4, "", 1L) }
        verify(exactly = 0) { reviewJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería devolver 404 para reservas inexistentes y 400 para datos inválidos")
    fun `createReview should handle missing reservations and invalid data`() {
        // Given
        every { reservationJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertThrows<NoSuchElementException> { reviewService.createReview("missing", 5, "", 1L) }
        assertThrows<IllegalArgumentException> { reviewService.createReview("  ", 5, "", 1L) }
        verify(exactly = 0) { reviewJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar valoraciones fuera de 1-5")
    fun `createReview should reject out-of-range ratings`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { reviewJpaRepository.existsByReservationId("res-1") } returns false

        // When / Then
        assertThrows<IllegalArgumentException> { reviewService.createReview("res-1", 0, "", 1L) }
        assertThrows<IllegalArgumentException> { reviewService.createReview("res-1", 6, "", 1L) }
        verify(exactly = 0) { reviewJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería retornar el detalle o null si no existe")
    fun `should return detail or null when missing`() {
        // Given
        every { reviewJpaRepository.findById("rev-1") } returns Optional.of(sampleReviewEntity())
        every { reviewJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertEquals("rev-1", reviewService.getReviewById("rev-1")?.id)
        assertNull(reviewService.getReviewById("missing"))
    }

    @Test
    @DisplayName("Debería paginar reseñas del hotel con su promedio")
    fun `getHotelReviews should return paginated reviews with average`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(sampleReviewEntity()), pageable, 1)
        every { reviewJpaRepository.findByHotelId("hotel-1", any<Pageable>()) } returns page
        every { reviewJpaRepository.averageRatingByHotelId("hotel-1") } returns 5.0

        // When
        val (reviews, total, average) = reviewService.getHotelReviews("hotel-1", 0, 10)

        // Then
        assertEquals(1, reviews.size)
        assertEquals(1L, total)
        assertEquals(5.0, average)
        assertEquals("rev-1", reviews.first().id)
    }

    @Test
    @DisplayName("Debería devolver página vacía y promedio 0.0 sin reseñas")
    fun `getHotelReviews should return empty page with zero average`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        every { reviewJpaRepository.findByHotelId("hotel-1", any<Pageable>()) } returns
            PageImpl(emptyList(), pageable, 0)
        every { reviewJpaRepository.averageRatingByHotelId("hotel-1") } returns 0.0

        // When
        val (reviews, total, average) = reviewService.getHotelReviews("hotel-1", 0, 10)

        // Then
        assertEquals(0, reviews.size)
        assertEquals(0L, total)
        assertEquals(0.0, average)
    }
}
