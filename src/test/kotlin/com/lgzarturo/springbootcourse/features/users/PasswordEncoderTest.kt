package com.lgzarturo.springbootcourse.features.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * PasswordEncoder Tests
 * Verifica el comportamiento actual (stub) del codificador de contraseñas.
 */
@DisplayName("PasswordEncoder Tests")
class PasswordEncoderTest {
    private val passwordEncoder = PasswordEncoder()

    @Test
    @DisplayName("Debería codificar la contraseña")
    fun `should encode password`() {
        val result = passwordEncoder.encode("Pikachu123!")

        assertEquals("Pikachu123!", result)
    }
}
