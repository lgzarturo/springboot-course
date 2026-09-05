package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
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

    @Test
    @DisplayName("Debería lanzar excepción cuando el nombre está vacío")
    fun `should throw exception when first name is blank`() {
        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("ash@pokemon.com"),
                password = Password("Pikachu123!"),
                firstName = " ",
                lastName = "Ketchum",
                phoneNumber = null,
                role = UserRole.GUEST,
            )
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el apellido está vacío")
    fun `should throw exception when last name is blank`() {
        assertThrows<IllegalArgumentException> {
            User.create(
                email = Email("ash@pokemon.com"),
                password = Password("Pikachu123!"),
                firstName = "Ash",
                lastName = " ",
                phoneNumber = null,
                role = UserRole.GUEST,
            )
        }
    }

    @Test
    @DisplayName("Debería cambiar la contraseña correctamente")
    fun `should change password successfully`() {
        // GIVEN
        val user = mockUser()
        val newPassword = Password("NewSecure1")

        // WHEN
        val updated = user.changePassword(newPassword)

        // THEN
        assertEquals(newPassword, updated.password)
    }

    @Test
    @DisplayName("Debería actualizar solo los campos proporcionados en el perfil")
    fun `should update only provided profile fields`() {
        // GIVEN
        val user = mockUser()

        // WHEN
        val updated = user.updateProfile(firstName = null, lastName = null, phoneNumber = null)

        // THEN
        assertEquals(user.firstName, updated.firstName)
        assertEquals(user.lastName, updated.lastName)
        assertEquals(user.phoneNumber, updated.phoneNumber)
    }

    @Test
    @DisplayName("Debería identificar correctamente el rol del usuario")
    fun `should identify user roles correctly`() {
        // GIVEN
        val admin =
            mockUser(role = UserRole.ADMIN)
        val staff =
            mockUser(role = UserRole.STAFF)
        val guest =
            mockUser(role = UserRole.GUEST)

        // THEN
        assertThat(admin.isAdmin()).isTrue()
        assertThat(admin.isStaff()).isFalse()
        assertThat(admin.isGuest()).isFalse()

        assertThat(staff.isAdmin()).isFalse()
        assertThat(staff.isStaff()).isTrue()
        assertThat(staff.isGuest()).isFalse()

        assertThat(guest.isAdmin()).isFalse()
        assertThat(guest.isStaff()).isFalse()
        assertThat(guest.isGuest()).isTrue()
    }
}
