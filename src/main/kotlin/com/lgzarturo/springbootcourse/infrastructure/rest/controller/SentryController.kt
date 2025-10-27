package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import io.sentry.Sentry
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST para la validación de Sentry
 * Punto de entrada HTTP para enviar un mensaje de prueba a Sentry
 */
@RestController
@RequestMapping("/api/v1/sentry")
class SentryController {

    /**
     * Endpoint para enviar un mensaje de prueba a Sentry
     * GET /api/v1/sentry/event
     * @return String con el mensaje "Sentry test completed!"
     */
    @GetMapping("/event")
    @Operation(
        summary = "",
        description = "",
    )
    fun test(): String {
        try {
            throw Exception("This is a test.")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
        return "Sentry event test completed!"
    }
}
