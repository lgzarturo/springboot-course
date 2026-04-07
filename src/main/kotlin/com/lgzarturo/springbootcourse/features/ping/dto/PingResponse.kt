package com.lgzarturo.springbootcourse.features.ping.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * DTO de respuesta para el endpoint de Ping
 * Representa la estructura JSON que se enviará al cliente
 */
data class PingResponse(
    @param:JsonProperty("message")
    val message: String,
    @param:JsonProperty("timestamp")
    val timestamp: LocalDateTime,
    @param:JsonProperty("version")
    val version: String,
)
