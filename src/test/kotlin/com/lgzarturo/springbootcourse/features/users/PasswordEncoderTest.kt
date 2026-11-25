package com.lgzarturo.springbootcourse.features.users

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("PasswordEncoder Tests")
class PasswordEncoderTest {
    private val passwordEncoder = PasswordEncoder()

    @Test
    @DisplayName("Debe usar BCrypt con cost 12 (H4)")
    fun `hashes use bcrypt cost 12`() {
        assertTrue(passwordEncoder.encode("Password123!").startsWith("\$2a\$12\$"))
    }

    @Test
    @DisplayName("Debe verificar la contraseña correcta y rechazar otra")
    fun `matches verifies raw password`() {
        val hash = passwordEncoder.encode("Password123!")

        assertTrue(passwordEncoder.matches("Password123!", hash))
        assertFalse(passwordEncoder.matches("WrongPass1", hash))
    }
}
