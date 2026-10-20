package com.lgzarturo.springbootcourse.features.reservations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

/**
 * Tests unitarios del modelo de dominio Reservation.
 * Verifica cálculo de precio, solape de fechas y máquina de estados sin Spring.
 */
@DisplayName("Reservation Domain Tests")
class ReservationTest {
    private fun sampleReservation(
        checkIn: LocalDate = LocalDate.of(2026, 11, 1),
        checkOut: LocalDate = LocalDate.of(2026, 11, 4),
        status: ReservationStatus = ReservationStatus.PENDING,
    ) = Reservation(
        id = "res-1",
        roomId = "room-1",
        hotelId = "hotel-1",
        userId = 1L,
        guestName = "Ash Ketchum",
        checkIn = checkIn,
        checkOut = checkOut,
        totalPrice = 450.0,
        status = status,
    )

    @Test
    @DisplayName("Debería calcular el total como noches por precio")
    fun `total price should equal nights multiplied by nightly price`() {
        // Given: 3 noches a 150.0
        val checkIn = LocalDate.of(2026, 11, 1)
        val checkOut = LocalDate.of(2026, 11, 4)

        // When
        val total = Reservation.calculateTotalPrice(150.0, checkIn, checkOut)

        // Then
        assertEquals(450.0, total)
    }

    @Test
    @DisplayName("Debería aceptar la cadena completa de transiciones válidas")
    fun `should accept the full chain of valid transitions`() {
        // Given
        val reservation = sampleReservation()

        // When / Then
        val confirmed = reservation.transitionTo(ReservationStatus.CONFIRMED)
        assertEquals(ReservationStatus.CONFIRMED, confirmed.status)
        val inProgress = confirmed.transitionTo(ReservationStatus.IN_PROGRESS)
        assertEquals(ReservationStatus.IN_PROGRESS, inProgress.status)
        val completed = inProgress.transitionTo(ReservationStatus.COMPLETED)
        assertEquals(ReservationStatus.COMPLETED, completed.status)
    }

    @Test
    @DisplayName("Debería permitir cancelar desde PENDING y CONFIRMED")
    fun `should allow cancellation from pending and confirmed`() {
        // Given
        val pending = sampleReservation(status = ReservationStatus.PENDING)
        val confirmed = sampleReservation(status = ReservationStatus.CONFIRMED)

        // When / Then
        assertEquals(ReservationStatus.CANCELLED, pending.transitionTo(ReservationStatus.CANCELLED).status)
        assertEquals(ReservationStatus.CANCELLED, confirmed.transitionTo(ReservationStatus.CANCELLED).status)
    }

    @Test
    @DisplayName("Debería rechazar rangos donde check-out no es posterior a check-in")
    fun `should reject ranges where check-out is not after check-in`() {
        // Given: mismo día y rango invertido
        val sameDay = sampleReservation(checkIn = LocalDate.of(2026, 11, 1), checkOut = LocalDate.of(2026, 11, 1))
        val inverted = sampleReservation(checkIn = LocalDate.of(2026, 11, 4), checkOut = LocalDate.of(2026, 11, 1))

        // When / Then
        assertThrows<IllegalArgumentException> { sameDay.validateDates() }
        assertThrows<IllegalArgumentException> { inverted.validateDates() }
    }

    @Test
    @DisplayName("Debería rechazar transiciones inválidas y estados finales")
    fun `should reject invalid transitions and final states`() {
        // Given
        val pending = sampleReservation(status = ReservationStatus.PENDING)
        val completed = sampleReservation(status = ReservationStatus.COMPLETED)
        val cancelled = sampleReservation(status = ReservationStatus.CANCELLED)

        // When / Then: salto directo, mismo estado y salidas desde finales
        assertThrows<IllegalStateException> { pending.transitionTo(ReservationStatus.IN_PROGRESS) }
        assertThrows<IllegalStateException> { pending.transitionTo(ReservationStatus.COMPLETED) }
        assertThrows<IllegalStateException> { completed.transitionTo(ReservationStatus.CANCELLED) }
        assertThrows<IllegalStateException> { cancelled.transitionTo(ReservationStatus.CONFIRMED) }
        assertThrows<IllegalStateException> { pending.transitionTo(ReservationStatus.PENDING) }
    }

    @Test
    @DisplayName("Debería detectar solape solo con intersección real de fechas")
    fun `should detect overlap only on real date intersection`() {
        // Given
        val reservation = sampleReservation()

        // When / Then: solape interior y envolvente
        assertTrue(reservation.overlaps(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 5)))
        assertTrue(reservation.overlaps(LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 2)))

        // When / Then: adyacencia exacta no solapa (rango semiabierto)
        assertFalse(reservation.overlaps(LocalDate.of(2026, 11, 4), LocalDate.of(2026, 11, 6)))
        assertFalse(reservation.overlaps(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 11, 1)))
    }
}
