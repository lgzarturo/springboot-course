package com.lgzarturo.springbootcourse.features.users

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * GetUserUseCase Tests
 * Verifica el caso de uso de obtención de usuarios.
 */
@DisplayName("GetUserUseCase Tests")
class GetUserUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val useCase = GetUserUseCase(userRepository)

    @Test
    @DisplayName("Debería ejecutar el caso de uso de obtención de usuario")
    fun `should return a user by id`() {
        every { userRepository.findById(1L) } returns mockUser()

        val result = useCase.execute(1L)

        assertEquals("ash@pokemon.com", result.email.value)
        assertEquals("Ash", result.firstName)
        assertEquals("Ketchum", result.lastName)
    }
}
