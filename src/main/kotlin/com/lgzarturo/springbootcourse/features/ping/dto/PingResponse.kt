package com.lgzarturo.springbootcourse.features.ping.dto

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * DTO de respuesta para el endpoint de Ping
 * Representa la estructura JSON que se enviará al cliente
 */
@Schema(description = "Respuesta del endpoint de ping con mensaje y versión")
data class PingResponse(
    @param:JsonProperty("message")
    @field:Schema(description = "Mensaje de respuesta", example = "pong")
    val message: String,
    @param:JsonProperty("timestamp")
    @field:Schema(
        description = "Momento de generación (UTC, ISO-8601)",
        type = "string",
        format = "date-time",
        example = "2026-10-17T12:00:00",
    )
    val timestamp: LocalDateTime,
    @param:JsonProperty("version")
    @field:Schema(description = "Versión de la aplicación", example = "0.1.0")
    val version: String,
)
