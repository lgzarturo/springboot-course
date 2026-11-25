package com.lgzarturo.springbootcourse.features.reservations

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Reservation(
    val id: String,
    val roomId: String,
    val hotelId: String,
    val userId: Long,
    val guestName: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val totalPrice: Double,
    val status: ReservationStatus = ReservationStatus.PENDING,
) {
    fun nights(): Long = ChronoUnit.DAYS.between(checkIn, checkOut)

    fun validateDates() {
        require(checkOut.isAfter(checkIn)) {
            "check-out debe ser posterior a check-in"
        }
    }

    fun overlaps(
        otherCheckIn: LocalDate,
        otherCheckOut: LocalDate,
    ): Boolean = checkIn.isBefore(otherCheckOut) && checkOut.isAfter(otherCheckIn)

    fun canTransitionTo(target: ReservationStatus): Boolean = VALID_TRANSITIONS[status].orEmpty().contains(target)

    fun transitionTo(target: ReservationStatus): Reservation {
        check(canTransitionTo(target)) {
            "Transición inválida de $status a $target"
        }
        return copy(status = target)
    }

    companion object {
        private val VALID_TRANSITIONS: Map<ReservationStatus, Set<ReservationStatus>> =
            mapOf(
                ReservationStatus.PENDING to setOf(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED),
                ReservationStatus.CONFIRMED to setOf(ReservationStatus.IN_PROGRESS, ReservationStatus.CANCELLED),
                ReservationStatus.IN_PROGRESS to setOf(ReservationStatus.COMPLETED),
                ReservationStatus.COMPLETED to emptySet(),
                ReservationStatus.CANCELLED to emptySet(),
            )

        fun calculateTotalPrice(
            nightlyPrice: Double,
            checkIn: LocalDate,
            checkOut: LocalDate,
        ): Double = ChronoUnit.DAYS.between(checkIn, checkOut) * nightlyPrice
    }
}
