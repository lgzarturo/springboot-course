package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * UserMapper Tests
 * Verifica la conversión de User de dominio a UserResponse
 */
@DisplayName("UserMapper Tests")
class UserMapperTest {
    private val userMapper = UserMapper()

    @Test
    @DisplayName("Debería convertir un User a UserResponse")
    fun `should map user to response`() {
        // GIVEN
        val user = mockUser()

        // WHEN
        val response = userMapper.toResponse(user)

        // THEN
        assertEquals(user.id.value, response.id)
        assertEquals("ash@pokemon.com", response.email)
        assertEquals("Ash", response.firstName)
        assertEquals("Ketchum", response.lastName)
        assertEquals("+14155552671", response.phoneNumber)
        assertEquals("GUEST", response.role)
    }

    @Test
    @DisplayName("Debería mapear teléfono nulo a UserResponse")
    fun `should map null phone number to response`() {
        // GIVEN
        val user =
            User.create(
                email = Email("ash@pokemon.com"),
                password = Password("Pikachu123!"),
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = null,
                role = UserRole.GUEST,
            )

        // WHEN
        val response = userMapper.toResponse(user)

        // THEN
        assertNull(response.phoneNumber)
    }
}
