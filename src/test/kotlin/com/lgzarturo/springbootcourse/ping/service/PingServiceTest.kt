package com.lgzarturo.springbootcourse.ping.service

import com.lgzarturo.springbootcourse.ping.domain.Ping
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Tests unitarios para PingService
 * Verifica la lógica de negocio del servicio de Ping
 */
@DisplayName("PingService Tests")
class PingServiceTest {
    private val pingService = PingService()

    @Test
    @DisplayName("Debe retornar un ping con mensaje 'pong'")
    fun `should return ping with pong message`() {
        // When
        val result = pingService.getPing()

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("pong", result.message)
        Assertions.assertNotNull(result.timestamp)
        Assertions.assertEquals("0.0.2", result.version)
    }

    @Test
    @DisplayName("Debe retornar un ping con mensaje personalizado")
    fun `should return ping with custom message`() {
        // Given
        val customMessage = "hello"

        // When
        val result = pingService.getPingWithMessage(customMessage)

        // Then
        Assertions.assertNotNull(result)
        Assertions.assertEquals("pong: hello", result.message)
        Assertions.assertNotNull(result.timestamp)
        Assertions.assertEquals("0.0.2", result.version)
    }

    @Test
    @DisplayName("Debe crear un objeto Ping válido")
    fun `should create valid ping object`() {
        // When
        val result = pingService.getPing()

        // Then
        Assertions.assertTrue(result is Ping)
        Assertions.assertNotNull(result.message)
        Assertions.assertNotNull(result.timestamp)
        Assertions.assertNotNull(result.version)
    }
}
