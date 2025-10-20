package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Ping
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertNotNull(result)
        assertEquals("pong", result.message)
        assertNotNull(result.timestamp)
        assertEquals("1.0.0", result.version)
    }

    @Test
    @DisplayName("Debe retornar un ping con mensaje personalizado")
    fun `should return ping with custom message`() {
        // Given
        val customMessage = "hello"

        // When
        val result = pingService.getPingWithMessage(customMessage)

        // Then
        assertNotNull(result)
        assertEquals("pong: hello", result.message)
        assertNotNull(result.timestamp)
        assertEquals("1.0.0", result.version)
    }

    @Test
    @DisplayName("Debe crear un objeto Ping válido")
    fun `should create valid ping object`() {
        // When
        val result = pingService.getPing()

        // Then
        assertTrue(result is Ping)
        assertNotNull(result.message)
        assertNotNull(result.timestamp)
        assertNotNull(result.version)
    }
}
