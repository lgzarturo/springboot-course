package com.lgzarturo.springbootcourse.features.users

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * UpdateUserUseCase Tests
 * Verifica el comportamiento actual (stub) del caso de uso de actualización de usuarios.
 * Nota: la implementación real está pendiente.
 */
@DisplayName("UpdateUserUseCase Tests")
class UpdateUserUseCaseTest {
    private val useCase = UpdateUserUseCase()

    @Test
    @DisplayName("Debería ejecutar el caso de uso de actualización de usuario")
    fun `should execute update user use case`() {
        // WHEN
        val result = useCase.execute(1L)

        // THEN
        assertNotNull(result)
        assertEquals("ash@pokemon.com", result.email.value)
        assertEquals("Ash", result.firstName)
        assertEquals("Ketchum", result.lastName)
        assertTrue(result.isActive)
    }
}
