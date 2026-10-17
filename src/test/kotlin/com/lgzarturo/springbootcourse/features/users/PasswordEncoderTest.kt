package com.lgzarturo.springbootcourse.features.users

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * PasswordEncoder Tests
 * Verifica que las contraseñas se codifican con BCrypt.
 */
@DisplayName("PasswordEncoder Tests")
class PasswordEncoderTest {
    private val passwordEncoder = PasswordEncoder()

    @Test
    @DisplayName("Debería codificar la contraseña")
    fun `should encode password with BCrypt`() {
        val result = passwordEncoder.encode("Pikachu123!")

        assertNotEquals("Pikachu123!", result)
        assertTrue(result.startsWith("\$2"))
    }
}
