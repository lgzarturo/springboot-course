package com.lgzarturo.springbootcourse.users.domain

import com.lgzarturo.springbootcourse.users.domain.valueobjects.Email
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Password
import com.lgzarturo.springbootcourse.users.domain.valueobjects.PhoneNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * User Domain Tests
 * Pruebas unitarias para la entidad User
 */
@DisplayName("User Domain Tests")
class UserTest {
    @Test
    @DisplayName("Debería crear un usuario con datos válidos")
    fun `should create user with valid data`() {
        // GIVEN
        val email = Email("ash@pokemon.com")
        val password = Password("Pikachu123!")

        // WHEN
        val user =
            User.create(
                email = email,
                password = password,
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = PhoneNumber("+14155552671"),
                role = UserRole.GUEST,
            )

        // THEN
        assertThat(user.email).isEqualTo(email)
        assertThat(user.fullName()).isEqualTo("Ash Ketchum")
        assertThat(user.isActive).isTrue
        assertThat(user.role).isEqualTo(UserRole.GUEST)
    }

    @Test
    @DisplayName("Debería lanzar excepción con correo electrónico inválido")
    fun `should throw exception with invalid email`() {
        assertThrows<IllegalArgumentException> {
            Email("invalid-email")
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción con contraseña débil")
    fun `should throw exception with weak password`() {
        assertThrows<IllegalArgumentException> {
            Password("weak")
        }
    }

    @Test
    @DisplayName("Debería actualizar correctamente el perfil del usuario")
    fun `should update profile successfully`() {
        // GIVEN
        val user = mockUser()

        // WHEN
        val updated =
            user.updateProfile(
                firstName = "Red",
                lastName = "Trainer",
                phoneNumber = PhoneNumber("+14155552672"),
            )

        // THEN
        assertEquals("Red", updated.firstName)
        assertEquals("Trainer", updated.lastName)
    }

    @Test
    @DisplayName("Debería desactivar correctamente el usuario")
    fun `should deactivate user`() {
        // GIVEN
        val user = mockUser()

        // WHEN
        val deactivated = user.deactivate()

        // THEN
        assertFalse(deactivated.isActive)
    }
}
