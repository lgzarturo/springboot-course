package com.lgzarturo.springbootcourse.features.users.exceptions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * DuplicateEmailException Tests
 * Verifica el mensaje de la excepción de correo duplicado
 */
@DisplayName("DuplicateEmailException Tests")
class DuplicateEmailExceptionTest {
    @Test
    @DisplayName("El mensaje debe ser genérico y no filtrar el correo (WS7)")
    fun `message must be generic without leaking email`() {
        val exception = DuplicateEmailException()

        assertEquals("Email already registered", exception.message)
    }

    @Test
    @DisplayName("Debería ser lanzable como RuntimeException")
    fun `should be throwable as runtime exception`() {
        assertThrows<DuplicateEmailException> {
            throw DuplicateEmailException()
        }
    }
}
