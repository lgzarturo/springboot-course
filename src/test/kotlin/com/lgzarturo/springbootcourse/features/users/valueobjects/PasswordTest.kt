package com.lgzarturo.springbootcourse.features.users.valueobjects

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Password Value Object Tests
 * Pruebas unitarias para el objeto de valor Password
 */
@DisplayName("Password Value Object Tests")
class PasswordTest {
    @ParameterizedTest
    @ValueSource(strings = ["Pikachu123!", "Abcdefg1", "Secure9Pass", "X1yZ2wV3u"])
    @DisplayName("Debería aceptar contraseñas seguras")
    fun `should accept secure passwords`(value: String) {
        val password = Password(value)

        assertEquals(value, password.value)
        assertTrue(Password.isSecure(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["weak", "nouppercase1", "NOLOWERCASE1", "NoDigitsHere", "Ab1", ""])
    @DisplayName("Debería rechazar contraseñas inseguras")
    fun `should reject insecure passwords`(value: String) {
        assertFalse(Password.isSecure(value))
        assertThrows<IllegalArgumentException> {
            Password(value)
        }
    }

    @Test
    @DisplayName("Debería rechazar contraseña sin dígito")
    fun `should reject password without digit`() {
        assertThrows<IllegalArgumentException> {
            Password("NoNumbers!")
        }
    }

    @Test
    @DisplayName("Debería rechazar contraseña sin mayúscula")
    fun `should reject password without uppercase`() {
        assertThrows<IllegalArgumentException> {
            Password("minuscula1")
        }
    }

    @Test
    @DisplayName("Debería rechazar contraseña sin minúscula")
    fun `should reject password without lowercase`() {
        assertThrows<IllegalArgumentException> {
            Password("MAYUSCULA1")
        }
    }

    @Test
    @DisplayName("Debería rechazar contraseña con menos de 8 caracteres")
    fun `should reject password shorter than 8 characters`() {
        assertThrows<IllegalArgumentException> {
            Password("Aa1")
        }
    }

    @Test
    @DisplayName("Debería crear un Password desde un valor encriptado")
    fun `should create password from encrypted value`() {
        val encrypted = "Pikachu123!"

        val password = Password.fromEncrypted(encrypted)

        assertEquals(encrypted, password.value)
    }
}
