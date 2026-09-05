package com.lgzarturo.springbootcourse.common.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * DateTimeExtensions Tests
 * Pruebas unitarias para las extensiones de LocalDateTime
 */
@DisplayName("DateTimeExtensions Tests")
class DateTimeExtensionsTest {
    private val dateTime: LocalDateTime = LocalDateTime.of(2024, 5, 17, 10, 30, 45, 123000000)

    @Test
    @DisplayName("Debería formatear un LocalDateTime a ISO")
    fun `should format LocalDateTime to ISO string`() {
        val result = dateTime.toIsoString()

        assertEquals("2024-05-17T10:30:45.123", result)
    }

    @Test
    @DisplayName("Debería formatear con el patrón por defecto")
    fun `should format with default pattern`() {
        val result = dateTime.toFormattedString()

        assertEquals("2024-05-17 10:30:45", result)
    }

    @Test
    @DisplayName("Debería formatear con un patrón personalizado")
    fun `should format with custom pattern`() {
        val result = dateTime.toFormattedString("dd/MM/yyyy")

        assertEquals("17/05/2024", result)
    }
}
