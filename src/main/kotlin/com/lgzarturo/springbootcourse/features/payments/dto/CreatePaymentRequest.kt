package com.lgzarturo.springbootcourse.features.payments.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Datos para crear un pago")
data class CreatePaymentRequest(
    @field:NotBlank(message = "La reserva no puede estar vacía")
    @field:Schema(
        description = "Identificador de la reserva CONFIRMED a pagar",
        example = "res-1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val reservationId: String,
    @field:Size(max = 50, message = "El método no puede exceder los 50 caracteres")
    @field:Schema(description = "Etiqueta del método simulado", example = "SIMULATED")
    val method: String = "SIMULATED",
    @field:Schema(description = "Fuerza un fallo del gateway simulado (solo pruebas)", example = "false")
    val simulateFailure: Boolean = false,
)
