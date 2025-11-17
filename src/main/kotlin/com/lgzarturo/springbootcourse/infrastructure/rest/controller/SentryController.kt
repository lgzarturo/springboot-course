package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.shared.exception.SentryTestException
import com.lgzarturo.springbootcourse.shared.constant.AppConstants
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST para la validación de Sentry
 * Punto de entrada HTTP para enviar un mensaje de prueba a Sentry
 */
@RestController
@RequestMapping("/api/v1/sentry")
@Tag(name = "Sentry", description = "Endpoints para testing de integración con Sentry")
class SentryController {
    /**
     * Endpoint para enviar un mensaje de prueba a Sentry
     * GET /api/v1/sentry/event
     * @return String con el mensaje "Sentry test completed!"
     */
    @GetMapping("/event")
    @Operation(
        summary = "Enviar evento de prueba a Sentry",
        description = "Genera una excepción de prueba y la envía a Sentry con contexto básico",
    )
    fun testEvent(): String {
        try {
            throw SentryTestException("This is a test.")
        } catch (e: SentryTestException) {
            Sentry.captureException(e)
        }
        return "Sentry event test completed!"
    }

    @GetMapping("/breadcrumbs")
    @Operation(
        summary = "Probar breadcrumbs",
        description = "Genera múltiples breadcrumbs antes de lanzar una excepción",
    )
    fun testBreadcrumbs(): String {
        // Agregar varios breadcrumbs para simular un flujo
        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                category = "user.action"
                message = "User started checkout process"
                level = SentryLevel.INFO
                setData("cart_items", AppConstants.SAMPLE_CART_ITEMS)
            },
        )

        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                category = "payment"
                message = "Payment method selected"
                level = SentryLevel.INFO
                setData("method", "credit_card")
            },
        )

        Sentry.addBreadcrumb(
            Breadcrumb().apply {
                category = "validation"
                message = "Validating payment information"
                level = SentryLevel.DEBUG
            },
        )

        try {
            throw SentryTestException("Payment validation failed - test exception")
        } catch (e: SentryTestException) {
            Sentry.captureException(e)
        }

        return "Breadcrumbs test completed! Check Sentry for the full context."
    }

    @GetMapping("/message")
    @Operation(
        summary = "Enviar mensaje personalizado",
        description = "Envía un mensaje a Sentry sin lanzar una excepción",
    )
    fun testMessage(
        @RequestParam(defaultValue = "Test message") message: String,
    ): String {
        Sentry.captureMessage(message, SentryLevel.INFO)
        return "Message sent to Sentry: $message"
    }

    @GetMapping("/transaction")
    @Operation(
        summary = "Probar performance monitoring",
        description = "Crea una transacción para medir performance",
    )
    fun testTransaction(): String {
        val transaction = Sentry.startTransaction("test-transaction", "http.server")

        try {
            // Simular operación 1
            val span1 = transaction.startChild("database.query", "fetch users")
            Thread.sleep(AppConstants.SAMPLE_OP_1)
            span1.finish()

            // Simular operación 2
            val span2 = transaction.startChild("http.client", "call external api")
            Thread.sleep(AppConstants.SAMPLE_OP_2)
            span2.finish()

            // Simular operación 3
            val span3 = transaction.startChild("cache.get", "fetch from redis")
            Thread.sleep(AppConstants.SAMPLE_OP_3)
            span3.finish()

            transaction.status = io.sentry.SpanStatus.OK
            throw SentryTestException("Transaction failed")
        } catch (e: SentryTestException) {
            transaction.status = io.sentry.SpanStatus.INTERNAL_ERROR
            transaction.throwable = e
            Sentry.captureException(e)
        } finally {
            transaction.finish()
        }

        return "Transaction test completed! Check Sentry Performance for details."
    }

    @GetMapping("/custom-fingerprint")
    @Operation(
        summary = "Probar fingerprint personalizado",
        description = "Envía eventos con fingerprint personalizado para agruparlos",
    )
    fun testCustomFingerprint(
        @RequestParam type: String,
    ): String {
        try {
            throw SentryTestException("Custom fingerprint test: $type")
        } catch (e: SentryTestException) {
            Sentry.captureException(e) { scope ->
                // Agrupar por tipo en lugar de por stack trace
                scope.fingerprint = listOf("custom-error", type)
                scope.setTag("error_category", type)
            }
        }

        return "Custom fingerprint test completed for type: $type"
    }
}
