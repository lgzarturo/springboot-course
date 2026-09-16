package com.lgzarturo.springbootcourse.features.users

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * CreateUserService Tests
 * Verifica el comportamiento actual (stub) del servicio de creación de usuarios.
 * Nota: la implementación real de persistencia está pendiente.
 */
@DisplayName("CreateUserService Tests")
class CreateUserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val service = CreateUserService(userRepository, passwordEncoder)

    @Test
    @DisplayName("Debería ejecutar el servicio de creación de usuario")
    fun `should execute create user service`() {
        // GIVEN
        val command =
            CreateUserCommand(
                email = "ash@pokemon.com",
                password = "Pikachu123!",
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = "+14155552671",
                role = UserRole.GUEST,
            )

        // WHEN
        val result = service.execute(command)

        // THEN
        assertEquals("ash@pokemon.com", result.email.value)
        assertEquals("Ash", result.firstName)
        assertEquals("Ketchum", result.lastName)
        assertEquals(UserRole.GUEST, result.role)
        assertTrue(result.isActive)
    }

    @Test
    @DisplayName("Debería aceptar un comando sin teléfono")
    fun `should accept command without phone number`() {
        // GIVEN
        val command =
            CreateUserCommand(
                email = "ash@pokemon.com",
                password = "Pikachu123!",
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = null,
                role = UserRole.STAFF,
            )

        // WHEN
        val result = service.execute(command)

        // THEN
        assertEquals("ash@pokemon.com", result.email.value)
    }
}
