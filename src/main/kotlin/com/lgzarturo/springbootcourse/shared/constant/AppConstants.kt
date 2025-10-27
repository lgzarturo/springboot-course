package com.lgzarturo.springbootcourse.shared.constant

/**
 * Constantes de la aplicación
 * Centraliza valores constantes utilizados en toda la aplicación
 */
object AppConstants {
    const val API_VERSION = "v1"
    const val API_BASE_PATH = "/api/$API_VERSION"
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_PAGE_SIZE = 100
    const val SAMPLE_CART_ITEMS = 3
    const val SAMPLE_OP_1: Long = 100
    const val SAMPLE_OP_2: Long = 150
    const val SAMPLE_OP_3: Long = 50
}

/**
 * Objeto que tiene la configuración de CORS
 */
object CorsConstants {
    const val MAX_AGE_SECONDS = 3600L
    const val API_PATH_PATTERN = "/api/**"
    val ALLOWED_ORIGINS = listOf("http://localhost:3000", "http://localhost:4200")
    val ALLOWED_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    val ALLOWED_HEADERS = listOf("*")
}

/**
 * Constantes de mensajes
 */
object Messages {
    const val PING_DEFAULT_MESSAGE = "pong"
    const val SERVICE_UNAVAILABLE = "El servicio no está disponible en este momento"
    const val VALIDATION_ERROR = "Error de validación en los datos proporcionados"
}
