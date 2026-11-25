package com.lgzarturo.springbootcourse.config.security

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.users.CreateUserCommand
import com.lgzarturo.springbootcourse.features.users.CreateUserService
import com.lgzarturo.springbootcourse.features.users.UserRole
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS5 (M1): CORS explícito integrado en la cadena de seguridad.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("CORS Integration Tests")
class CorsIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Autowired
    private lateinit var authService: AuthService

    @Test
    @DisplayName("Preflight desde origen permitido debe responder 200 con Allow-Origin")
    fun `preflight from allowed origin succeeds`() {
        mockMvc
            .perform(
                options("/api/v1/users")
                    .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"),
            ).andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
    }

    @Test
    @DisplayName("Preflight desde origen no permitido debe rechazarse sin Allow-Origin")
    fun `preflight from denied origin is rejected`() {
        mockMvc
            .perform(
                options("/api/v1/users")
                    .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"),
            ).andExpect(status().isForbidden)
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
    }

    @Test
    @DisplayName("Petición real con Origin permitido debe incluir Allow-Origin")
    fun `actual request echoes allowed origin`() {
        val token = loginAs("cors.actual", UserRole.GUEST)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token")
                    .header(HttpHeaders.ORIGIN, "http://localhost:4200"),
            ).andExpect(status().isOk)
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
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
                firstName = "Cors",
                lastName = "Test",
                phoneNumber = null,
                role = role,
            ),
        )
        return authService.authenticate(email, "Password123!")
    }
}
