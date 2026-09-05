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
 * Email Value Object Tests
 * Pruebas unitarias para el objeto de valor Email
 */
@DisplayName("Email Value Object Tests")
class EmailTest {
    @ParameterizedTest
    @ValueSource(strings = ["ash@pokemon.com", "user.name+tag@example.co", "a@b.io"])
    @DisplayName("Debería aceptar correos electrónicos válidos")
    fun `should accept valid emails`(value: String) {
        val email = Email(value)

        assertEquals(value, email.value)
        assertTrue(Email.isValid(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["invalid-email", "@nodomain.com", "user@", "user@.com", "user @domain.com", ""])
    @DisplayName("Debería rechazar correos electrónicos inválidos")
    fun `should reject invalid emails`(value: String) {
        assertFalse(Email.isValid(value))
        assertThrows<IllegalArgumentException> {
            Email(value)
        }
    }

    @Test
    @DisplayName("Debería rechazar correo sin dominio de nivel superior")
    fun `should reject email without top level domain`() {
        assertFalse(Email.isValid("user@domain"))
    }
}
