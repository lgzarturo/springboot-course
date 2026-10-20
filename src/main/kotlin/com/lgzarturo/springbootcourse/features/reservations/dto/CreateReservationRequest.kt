package com.lgzarturo.springbootcourse.features.reservations.dto

import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "Datos para crear una reserva")
data class CreateReservationRequest(
    @field:NotBlank(message = "La habitación no puede estar vacía")
    @field:Schema(
        description = "Identificador de la habitación",
        example = "room-1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val roomId: String,
    @field:NotNull(message = "El usuario no puede estar vacío")
    @field:Schema(description = "Identificador del usuario", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    val userId: Long?,
    @field:NotBlank(message = "El nombre del huésped no puede estar vacío")
    @field:Size(max = 255, message = "El nombre del huésped no puede exceder los 255 caracteres")
    @field:Schema(
        description = "Nombre del huésped",
        example = "Ash Ketchum",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val guestName: String,
    @field:NotNull(message = "La fecha de check-in no puede estar vacía")
    @field:Schema(
        description = "Fecha de check-in",
        example = "2026-11-01",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val checkIn: LocalDate?,
    @field:NotNull(message = "La fecha de check-out no puede estar vacía")
    @field:Schema(
        description = "Fecha de check-out",
        example = "2026-11-04",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val checkOut: LocalDate?,
    @field:Schema(description = "Estado inicial (siempre PENDING)", example = "PENDING")
    val status: ReservationStatus = ReservationStatus.PENDING,
)
