package com.lgzarturo.springbootcourse.features.users

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * UpdateUserUseCase Tests
 * Verifica el caso de uso de actualización de usuarios.
 */
@DisplayName("UpdateUserUseCase Tests")
class UpdateUserUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val useCase = UpdateUserUseCase(userRepository)

    @Test
    @DisplayName("Debería ejecutar el caso de uso de actualización de usuario")
    fun `should update a user profile`() {
        every { userRepository.findById(1L) } returns mockUser()
        every { userRepository.save(any()) } answers { firstArg() }

        val result =
            useCase.execute(
                1L,
                UpdateUserCommand(
                    firstName = "Red",
                    lastName = "Trainer",
                    phoneNumber = "+14155552672",
                ),
            )

        assertEquals("Red", result.firstName)
        assertEquals("Trainer", result.lastName)
    }
}
