package com.lgzarturo.springbootcourse.features.payments

import com.lgzarturo.springbootcourse.features.payments.dto.CreatePaymentRequest
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@TestConfiguration
class PaymentMockkTestConfig {
    @Bean
    fun paymentService(): PaymentService = mockk()
}

/**
 * Tests del controlador REST de pagos.
 * Verifica creación, detalle, listado con filtros, reembolsos y errores.
 */
@WebMvcTest(PaymentController::class)
@Import(PaymentMockkTestConfig::class, SecurityTestConfig::class)
@DisplayName("PaymentController Tests")
class PaymentControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = "/api/v1/payments"

    private fun createRequest() = CreatePaymentRequest(reservationId = "res-1")

    private fun samplePayment(status: PaymentStatus = PaymentStatus.COMPLETED) =
        Payment(
            id = "pay-1",
            reservationId = "res-1",
            amount = 450.0,
            status = status,
            method = "SIMULATED",
            gatewayReference = "sim-1",
        )

    @BeforeEach
    fun setUp() {
        clearMocks(paymentService)
    }

    @Test
    @DisplayName("POST debe crear un pago y devolver 201 con el importe de la reserva")
    fun `should create payment and return 201`() {
        // Given
        every { paymentService.createPayment(any(), any(), any()) } returns samplePayment()

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", `is`("pay-1")))
            .andExpect(jsonPath("$.reservationId", `is`("res-1")))
            .andExpect(jsonPath("$.amount", `is`(450.0)))
            .andExpect(jsonPath("$.status", `is`("COMPLETED")))
    }

    @Test
    @DisplayName("POST debe devolver 400 con datos inválidos")
    fun `should return 400 with invalid payment data`() {
        // Given: reserva en blanco
        val invalid = createRequest().copy(reservationId = " ")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("POST debe devolver 409 con reserva no confirmada y 404 si no existe")
    fun `should return 409 on wrong status and 404 on missing reservation`() {
        // Given: reserva no confirmada
        every { paymentService.createPayment(any(), any(), any()) } throws
            IllegalStateException("Solo se puede pagar una reserva CONFIRMED")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isConflict)

        // Given: reserva inexistente
        every { paymentService.createPayment(any(), any(), any()) } throws
            NoSuchElementException("Reserva no encontrada: missing")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET debe devolver el detalle o 404 si no existe")
    fun `should return detail or 404 when missing`() {
        // Given
        every { paymentService.getPaymentById("pay-1") } returns samplePayment()
        every { paymentService.getPaymentById("missing") } returns null

        // When / Then
        mockMvc
            .perform(get("$baseUrl/pay-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", `is`("pay-1")))
            .andExpect(jsonPath("$.gatewayReference", `is`("sim-1")))

        mockMvc
            .perform(get("$baseUrl/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET listado debe paginar y filtrar por reserva y estado")
    fun `should list payments paginated and filtered`() {
        // Given
        every { paymentService.getAllPayments(any(), any(), any()) } returns
            (listOf(samplePayment()) to 1L)

        // When / Then
        mockMvc
            .perform(get("$baseUrl?reservationId=res-1&status=COMPLETED&page=0&size=10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.totalElements", `is`(1)))
    }

    @Test
    @DisplayName("POST refund debe reembolsar o devolver 404 y 409 según el caso")
    fun `should refund or return 404 and 409`() {
        // Given: reembolso válido
        every { paymentService.refundPayment("pay-1") } returns samplePayment(status = PaymentStatus.REFUNDED)
        every { paymentService.refundPayment("missing") } returns null
        every { paymentService.refundPayment("pay-failed") } throws
            IllegalStateException("Solo se puede reembolsar un pago COMPLETED")

        // When / Then
        mockMvc
            .perform(post("$baseUrl/pay-1/refund"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("REFUNDED")))

        mockMvc
            .perform(post("$baseUrl/missing/refund"))
            .andExpect(status().isNotFound)

        mockMvc
            .perform(post("$baseUrl/pay-failed/refund"))
            .andExpect(status().isConflict)
    }
}
