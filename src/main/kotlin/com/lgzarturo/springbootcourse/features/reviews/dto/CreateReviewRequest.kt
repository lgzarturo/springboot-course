package com.lgzarturo.springbootcourse.features.reviews.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Datos para crear una reseña")
data class CreateReviewRequest(
    @field:NotBlank(message = "La reserva no puede estar vacía")
    @field:Schema(
        description = "Identificador de la reserva COMPLETED a valorar",
        example = "res-1",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val reservationId: String,
    @field:Min(value = 1, message = "La valoración mínima es 1")
    @field:Max(value = 5, message = "La valoración máxima es 5")
    @field:Schema(
        description = "Valoración de 1 a 5",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val rating: Int,
    @field:Size(max = 2000, message = "El comentario no puede exceder los 2000 caracteres")
    @field:Schema(description = "Comentario opcional", example = "Estancia excelente")
    val comment: String = "",
)
