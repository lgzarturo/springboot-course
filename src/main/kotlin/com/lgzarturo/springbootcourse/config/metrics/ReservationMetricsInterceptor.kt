package com.lgzarturo.springbootcourse.config.metrics

import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.concurrent.TimeUnit

/**
 * Registra contadores y timers de Micrometer para los endpoints de reservas.
 * Solo usa etiquetas acotadas (method, outcome): sin IDs ni datos personales.
 */
@Component
class ReservationMetricsInterceptor(
    private val meterRegistry: MeterRegistry,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val counterName = counterNameFor(request) ?: return
        val outcome = if (response.status in SUCCESS_RANGE) SUCCESS_OUTCOME else ERROR_OUTCOME
        meterRegistry.counter(counterName, OUTCOME_TAG, outcome).increment()
        val start = request.getAttribute(START_TIME_ATTRIBUTE) as? Long ?: return
        meterRegistry
            .timer(TIMER_NAME, METHOD_TAG, request.method, OUTCOME_TAG, outcome)
            .record(System.nanoTime() - start, TimeUnit.NANOSECONDS)
    }

    private fun counterNameFor(request: HttpServletRequest): String? =
        when {
            request.method == POST_METHOD && request.requestURI == RESERVATIONS_PATH -> CREATED_COUNTER
            request.method == PATCH_METHOD &&
                request.requestURI.startsWith(RESERVATIONS_PREFIX) &&
                request.requestURI.endsWith(STATUS_SUFFIX) -> STATUS_COUNTER
            else -> null
        }

    companion object {
        private const val START_TIME_ATTRIBUTE = "observability.startTime"
        private const val CREATED_COUNTER = "reservations.created"
        private const val STATUS_COUNTER = "reservations.status.changed"
        private const val TIMER_NAME = "reservations.request.duration"
        private const val RESERVATIONS_PATH = "/api/v1/reservations"
        private const val RESERVATIONS_PREFIX = "/api/v1/reservations/"
        private const val STATUS_SUFFIX = "/status"
        private const val POST_METHOD = "POST"
        private const val PATCH_METHOD = "PATCH"
        private const val OUTCOME_TAG = "outcome"
        private const val METHOD_TAG = "method"
        private const val SUCCESS_OUTCOME = "success"
        private const val ERROR_OUTCOME = "error"
        private val SUCCESS_RANGE = 200..299
    }
}
