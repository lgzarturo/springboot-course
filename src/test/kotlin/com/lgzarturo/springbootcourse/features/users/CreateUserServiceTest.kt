package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.exceptions.DuplicateEmailException
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * CreateUserService Tests
 * Verifica el servicio de creación de usuarios.
 */
@DisplayName("CreateUserService Tests")
class CreateUserServiceTest {
    private val encodedPassword =
        "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var service: CreateUserService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        service = CreateUserService(userRepository, passwordEncoder)
    }

    @Test
    @DisplayName("Debería ejecutar el servicio de creación de usuario")
    fun `should create and persist a user with an encoded password`() {
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

        every { userRepository.existsByEmail(command.email) } returns false
        every { passwordEncoder.encode(command.password) } returns encodedPassword
        every { userRepository.save(any()) } answers { firstArg() }

        val result = service.execute(command)

        assertEquals("ash@pokemon.com", result.email.value)
        assertEquals("Ash", result.firstName)
        assertEquals("Ketchum", result.lastName)
        assertEquals(UserRole.GUEST, result.role)
        assertEquals(encodedPassword, result.password.value)
        verify { userRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería aceptar un comando sin teléfono")
    fun `should reject a duplicate email`() {
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

        every { userRepository.existsByEmail(command.email) } returns true

        assertThrows<DuplicateEmailException> {
            service.execute(command)
        }
        verify(exactly = 0) { userRepository.save(any()) }
    }
}
