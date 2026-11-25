package com.lgzarturo.springbootcourse.features.reservations.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.features.reservations.Reservation
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Representación pública de una reserva")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReservationResponse(
    @field:Schema(description = "Identificador de la reserva", example = "res-1")
    val id: String? = null,
    @field:Schema(description = "Identificador de la habitación", example = "room-1")
    val roomId: String = "",
    @field:Schema(description = "Identificador del hotel", example = "hotel-1")
    val hotelId: String = "",
    @field:Schema(description = "Identificador del usuario", example = "1")
    val userId: Long = 0,
    @field:Schema(description = "Nombre del huésped", example = "Ash Ketchum")
    val guestName: String = "",
    @field:Schema(description = "Fecha de check-in", example = "2026-11-01")
    val checkIn: LocalDate? = null,
    @field:Schema(description = "Fecha de check-out", example = "2026-11-04")
    val checkOut: LocalDate? = null,
    @field:Schema(description = "Precio total de la estancia", example = "450.0")
    val totalPrice: Double = 0.0,
    @field:Schema(description = "Estado de la reserva", example = "PENDING")
    val status: ReservationStatus = ReservationStatus.PENDING,
) {
    companion object {
        fun fromDomain(reservation: Reservation): ReservationResponse =
            ReservationResponse(
                id = reservation.id.ifEmpty { null },
                roomId = reservation.roomId,
                hotelId = reservation.hotelId,
                userId = reservation.userId,
                guestName = reservation.guestName,
                checkIn = reservation.checkIn,
                checkOut = reservation.checkOut,
                totalPrice = reservation.totalPrice,
                status = reservation.status,
            )
    }
}
