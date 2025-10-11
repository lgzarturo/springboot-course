package com.lgzarturo.springbootcourse.domain.model

import java.time.LocalDateTime

/**
 * Modelo de dominio para representar un Ping
 * Este modelo es independiente de cualquier framework o tecnología
 */
data class Ping(
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val version: String = "1.0.0"
)
