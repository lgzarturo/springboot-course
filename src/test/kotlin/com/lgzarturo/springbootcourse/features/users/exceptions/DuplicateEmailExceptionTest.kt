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
    @DisplayName("Debería construir el mensaje con el correo duplicado")
    fun `should build message with duplicated email`() {
        val exception = DuplicateEmailException("ash@pokemon.com")

        assertEquals("Email already exists: ash@pokemon.com", exception.message)
    }

    @Test
    @DisplayName("Debería ser lanzable como RuntimeException")
    fun `should be throwable as runtime exception`() {
        assertThrows<DuplicateEmailException> {
            throw DuplicateEmailException("ash@pokemon.com")
        }
    }
}
