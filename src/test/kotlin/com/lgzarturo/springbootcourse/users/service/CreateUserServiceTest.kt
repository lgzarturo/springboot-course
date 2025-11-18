package com.lgzarturo.springbootcourse.users.service

import com.lgzarturo.springbootcourse.users.adapters.persistence.UserRepository
import com.lgzarturo.springbootcourse.users.domain.CreateUserCommand
import com.lgzarturo.springbootcourse.users.domain.UserRole
import com.lgzarturo.springbootcourse.users.domain.exceptions.DuplicateEmailException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class CreateUserServiceTest {
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val service = CreateUserService(userRepository, passwordEncoder)

    @Test
    fun `should create user successfully`() {
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

        every { userRepository.existsByEmail(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encrypted_password"
        every { userRepository.save(any()) } answers { firstArg() }

        // WHEN
        val result = service.execute(command)

        // THEN
        assertEquals("ash@pokemon.com", result.email.value)
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw exception when email already exists`() {
        // GIVEN
        val command =
            CreateUserCommand(
                email = "ash@pokemon.com",
                password = "Pikachu123!",
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = null,
                role = UserRole.GUEST,
            )

        every { userRepository.existsByEmail(any()) } returns true

        // WHEN & THEN
        assertThrows<DuplicateEmailException> {
            service.execute(command)
        }
    }
}
