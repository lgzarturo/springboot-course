package com.lgzarturo.springbootcourse.infrastructure.exception

/**
 * Excepción personalizada para pruebas de Sentry
 * Se utiliza para simular eventos de error y validar la integración con Sentry
 */
class SentryTestException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
