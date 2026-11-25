package com.lgzarturo.springbootcourse.config.security

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.users.CreateUserCommand
import com.lgzarturo.springbootcourse.features.users.CreateUserService
import com.lgzarturo.springbootcourse.features.users.UserRole
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS5 (M2): security headers en cada respuesta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Security Headers Integration Tests")
class SecurityHeadersIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Autowired
    private lateinit var authService: AuthService

    @Test
    @DisplayName("Las respuestas deben incluir headers anti-XSS, clickjacking y MIME")
    fun `responses include baseline security headers`() {
        val token = loginAs("headers.base", UserRole.GUEST)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
            .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
    }

    @Test
    @DisplayName("Las respuestas HTTPS deben incluir HSTS")
    fun `secure responses include hsts`() {
        val token = loginAs("headers.hsts", UserRole.GUEST)
        val secure =
            RequestPostProcessor { request ->
                request.isSecure = true
                request
            }

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token")
                    .with(secure),
            ).andExpect(status().isOk)
            .andExpect(header().string("Strict-Transport-Security", containsString("max-age=")))
    }

    private fun loginAs(
        prefix: String,
        role: UserRole,
    ): String {
        val email = "$prefix.${System.nanoTime()}@pokemon.com"
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Headers",
                lastName = "Test",
                phoneNumber = null,
                role = role,
            ),
        )
        return authService.authenticate(email, "Password123!")
    }
}
