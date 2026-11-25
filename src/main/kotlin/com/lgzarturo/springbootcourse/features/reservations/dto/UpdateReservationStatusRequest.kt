package com.lgzarturo.springbootcourse.features.reservations.dto

import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "Datos para cambiar el estado de una reserva")
data class UpdateReservationStatusRequest(
    @field:NotNull(message = "El estado no puede estar vacío")
    @field:Schema(description = "Nuevo estado", example = "CONFIRMED", requiredMode = Schema.RequiredMode.REQUIRED)
    val status: ReservationStatus?,
)
