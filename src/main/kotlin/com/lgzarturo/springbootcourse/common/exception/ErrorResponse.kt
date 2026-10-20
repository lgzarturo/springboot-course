package com.lgzarturo.springbootcourse.common.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Clase de respuesta estándar para errores
 * Proporciona una estructura consistente para todos los errores de la API
 */
@Schema(description = "Respuesta estándar de error de la API")
data class ErrorResponse(
    @field:Schema(
        description = "Momento en que ocurrió el error (UTC, ISO-8601)",
        type = "string",
        format = "date-time",
        example = "2026-10-17T12:00:00",
    )
    val timestamp: LocalDateTime = LocalDateTime.now(),
    @field:Schema(description = "Código de estado HTTP", example = "400")
    val status: Int,
    @field:Schema(description = "Nombre corto del error", example = "Bad Request")
    val error: String,
    @field:Schema(description = "Mensaje legible del error", example = "Los datos proporcionados no son válidos")
    val message: String,
    @field:Schema(description = "Ruta solicitada que produjo el error", example = "/api/v1/users")
    val path: String,
    @field:Schema(description = "Detalle de errores de validación por campo, si aplica")
    val details: List<String>? = null,
    @field:Schema(description = "Errores de validación indexados por campo, si aplica")
    val errors: Map<String, String>? = null,
)
