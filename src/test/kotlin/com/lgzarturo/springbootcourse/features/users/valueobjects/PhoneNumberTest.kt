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
 * PhoneNumber Value Object Tests
 * Pruebas unitarias para el objeto de valor PhoneNumber
 */
@DisplayName("PhoneNumber Value Object Tests")
class PhoneNumberTest {
    @ParameterizedTest
    @ValueSource(strings = ["+14155552671", "14155552671", "12", "+521234567890123"])
    @DisplayName("Debería aceptar números de teléfono válidos")
    fun `should accept valid phone numbers`(value: String) {
        val phoneNumber = PhoneNumber(value)

        assertEquals(value, phoneNumber.value)
        assertTrue(PhoneNumber.isValid(value))
    }

    @ParameterizedTest
    @ValueSource(strings = ["invalid-phone", "1", "0123", "+", "123456789012345678", "phone123", ""])
    @DisplayName("Debería rechazar números de teléfono inválidos")
    fun `should reject invalid phone numbers`(value: String) {
        assertFalse(PhoneNumber.isValid(value))
        assertThrows<IllegalArgumentException> {
            PhoneNumber(value)
        }
    }

    @Test
    @DisplayName("Debería rechazar número que inicia con cero")
    fun `should reject phone number starting with zero`() {
        assertThrows<IllegalArgumentException> {
            PhoneNumber("0123456789")
        }
    }

    @Test
    @DisplayName("Debería rechazar número demasiado largo")
    fun `should reject phone number too long`() {
        assertThrows<IllegalArgumentException> {
            PhoneNumber("+1234567890123456789")
        }
    }
}
