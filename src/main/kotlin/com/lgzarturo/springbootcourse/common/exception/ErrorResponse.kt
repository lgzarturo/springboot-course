package com.lgzarturo.springbootcourse.common.exception

import java.time.LocalDateTime

/**
 * Clase de respuesta estándar para errores
 * Proporciona una estructura consistente para todos los errores de la API
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val details: List<String>? = null,
    val errors: Map<String, String>? = null,
)
