package com.lgzarturo.springbootcourse.common.exception

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GlobalExceptionHandler Sentry Tests")
class GlobalExceptionHandlerSentryTest {
    private val handler = GlobalExceptionHandler()
    private val request = mockk<HttpServletRequest>()

    @BeforeEach
    fun setup() {
        mockkStatic(Sentry::class)
        every { request.requestURI } returns "/api/v1/reservations"
        every { Sentry.captureException(any<Throwable>()) } returns SentryId()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Sentry::class)
    }

    @Test
    @DisplayName("debe_reportar_a_sentry_cuando_excepcion_no_es_manejada")
    fun `debe_reportar_a_sentry_cuando_excepcion_no_es_manejada`() {
        val response = handler.handleGenericException(RuntimeException("boom"), request)

        assertEquals(500, response.statusCode.value())
        verify(exactly = 1) { Sentry.captureException(any<Throwable>()) }
    }
}
