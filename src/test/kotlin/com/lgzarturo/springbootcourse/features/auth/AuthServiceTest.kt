package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.auth.exceptions.InvalidCredentialsException
import com.lgzarturo.springbootcourse.features.users.PasswordEncoder
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import com.lgzarturo.springbootcourse.features.users.mockUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("AuthService Tests")
class AuthServiceTest {
    private val userRepository = mockk<UserDomainRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtService = mockk<JwtService>()
    private val authService = AuthService(userRepository, passwordEncoder, jwtService)

    @Test
    @DisplayName("Usuario inexistente debe verificar un hash dummy (anti-timing M7)")
    fun `unknown user still verifies dummy hash`() {
        every { userRepository.findByEmail(any()) } returns null
        every { passwordEncoder.matches(any(), any()) } returns false

        assertThrows<InvalidCredentialsException> {
            authService.authenticate("ghost@pokemon.com", "Password123!")
        }

        verify { passwordEncoder.matches("Password123!", any()) }
    }

    @Test
    @DisplayName("Credenciales válidas deben retornar un JWT")
    fun `valid credentials return jwt`() {
        val user = mockUser()
        every { userRepository.findByEmail("ash@pokemon.com") } returns user
        every { passwordEncoder.matches(any(), any()) } returns true
        every { jwtService.generateAccessToken(any(), any(), any(), any()) } returns "signed-jwt"

        assertEquals("signed-jwt", authService.authenticate("ash@pokemon.com", "Pikachu123!"))
    }

    @Test
    @DisplayName("Usuario desactivado debe retornar 401 sin emitir token")
    fun `deactivated user is rejected without token`() {
        val user = mockUser().deactivate()
        every { userRepository.findByEmail("ash@pokemon.com") } returns user
        every { passwordEncoder.matches(any(), any()) } returns true

        assertThrows<InvalidCredentialsException> {
            authService.authenticate("ash@pokemon.com", "Pikachu123!")
        }

        verify(exactly = 0) { jwtService.generateAccessToken(any(), any(), any(), any()) }
    }
}
