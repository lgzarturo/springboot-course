package com.lgzarturo.springbootcourse.features.sentry

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import io.sentry.Breadcrumb
import io.sentry.ITransaction
import io.sentry.Sentry
import io.sentry.SentryLevel
import io.sentry.SpanStatus
import io.sentry.protocol.SentryId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.util.HtmlUtils

/**
 * Tests de integración para SentryController
 * Verifica los endpoints de prueba de Sentry con mocks estáticos
 */
@WebMvcTest(SentryController::class)
@DisplayName("SentryController Tests")
class SentryControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockkStatic(Sentry::class)
        mockkStatic(HtmlUtils::class)
        every { Sentry.captureException(ofType(Throwable::class)) } returns SentryId.EMPTY_ID
        every { Sentry.captureException(ofType(Throwable::class), any<io.sentry.ScopeCallback>()) } returns
            SentryId.EMPTY_ID
        every { Sentry.addBreadcrumb(ofType(Breadcrumb::class)) } just runs
        every { Sentry.captureMessage(any<String>(), ofType(SentryLevel::class)) } returns SentryId.EMPTY_ID

        val mockTransaction = mockk<ITransaction>(relaxed = true)
        val mockSpan = mockk<io.sentry.ISpan>(relaxed = true)
        every { mockTransaction.startChild(any<String>(), any<String>()) } returns mockSpan
        every { mockTransaction.finish() } just runs
        every { mockSpan.finish() } just runs
        every { Sentry.startTransaction(any<String>(), any<String>()) } returns mockTransaction

        every { HtmlUtils.htmlEscape(any<String>()) } answers { firstArg<String>() }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Sentry::class)
        unmockkStatic(HtmlUtils::class)
    }

    @Test
    @DisplayName("GET /api/v1/sentry/event debe retornar mensaje de evento")
    fun `should return event test message`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/event"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Sentry event test completed!"))

        verify { Sentry.captureException(ofType(Throwable::class)) }
    }

    @Test
    @DisplayName("GET /api/v1/sentry/breadcrumbs debe retornar mensaje de breadcrumbs")
    fun `should return breadcrumbs test message`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/breadcrumbs"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    "Breadcrumbs test completed! Check Sentry for the full context.",
                ),
            )

        verify(exactly = 3) { Sentry.addBreadcrumb(ofType(Breadcrumb::class)) }
        verify { Sentry.captureException(ofType(Throwable::class)) }
    }

    @Test
    @DisplayName("GET /api/v1/sentry/message debe retornar mensaje personalizado")
    fun `should return custom message`() {
        every { HtmlUtils.htmlEscape("hello") } returns "hello"

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/message").param("message", "hello"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Message sent to Sentry: hello"))

        verify { Sentry.captureMessage("hello", ofType(SentryLevel::class)) }
    }

    @Test
    @DisplayName("GET /api/v1/sentry/message con valor por defecto")
    fun `should return default message when no param`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/message"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Message sent to Sentry: Test message"))
    }

    @Test
    @DisplayName("GET /api/v1/sentry/transaction debe retornar mensaje de transacción")
    fun `should return transaction test message`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/transaction"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().string(
                    "Transaction test completed! Check Sentry Performance for details.",
                ),
            )

        verify { Sentry.startTransaction("test-transaction", "http.server") }
        verify { Sentry.captureException(ofType(Throwable::class)) }
    }

    @Test
    @DisplayName("GET /api/v1/sentry/custom-fingerprint debe retornar mensaje con tipo")
    fun `should return custom fingerprint message`() {
        every { HtmlUtils.htmlEscape("payment") } returns "payment"

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/v1/sentry/custom-fingerprint").param("type", "payment"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("Custom fingerprint test completed for type: payment"))

        verify { Sentry.captureException(ofType(Throwable::class), any<io.sentry.ScopeCallback>()) }
    }
}
