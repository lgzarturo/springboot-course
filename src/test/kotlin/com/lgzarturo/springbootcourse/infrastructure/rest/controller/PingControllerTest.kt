package com.lgzarturo.springbootcourse.infrastructure.rest.controller

import com.lgzarturo.springbootcourse.domain.model.Ping
import com.lgzarturo.springbootcourse.domain.port.input.PingUseCase
import com.lgzarturo.springbootcourse.infrastructure.rest.mapper.PingMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Tests de integración para PingController
 * Verifica el comportamiento de los endpoints REST
 */
@WebMvcTest(PingController::class)
@Import(PingMapper::class)
@DisplayName("PingController Integration Tests")
class PingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var pingUseCase: PingUseCase

    @Autowired  // Usa el mapper real
    private lateinit var pingMapper: PingMapper

    @Test
    @DisplayName("GET /api/v1/ping debe retornar 200 OK con mensaje pong")
    fun `should return pong message when calling ping endpoint`() {
        // Given
        val ping = Ping(message = "pong")
        every { pingUseCase.getPing() } returns ping

        // When & Then
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("pong"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @DisplayName("GET /api/v1/ping/{message} debe retornar mensaje personalizado")
    fun `should return custom message when calling ping with message`() {
        // Given
        val customMessage = "hello"
        val ping = Ping(message = "pong: $customMessage")
        every { pingUseCase.getPingWithMessage(customMessage) } returns ping

        // When & Then
        mockMvc.perform(get("/api/v1/ping/$customMessage"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message").value("pong: hello"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    @DisplayName("GET /api/v1/ping/health debe retornar status UP")
    fun `should return UP status when calling health endpoint`() {
        // When & Then
        mockMvc.perform(get("/api/v1/ping/health"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("springboot-course"))
    }
}
