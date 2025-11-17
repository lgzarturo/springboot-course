package com.lgzarturo.springbootcourse.ping.domain

import java.time.LocalDateTime

/**
 * Modelo de dominio para representar un Ping
 * Este modelo es independiente de cualquier framework o tecnología
 */
data class Ping(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String = "0.0.2",
) {
    init {
        require(message.isNotBlank()) { "El mensaje no puede estar vacío" }
    }
}
