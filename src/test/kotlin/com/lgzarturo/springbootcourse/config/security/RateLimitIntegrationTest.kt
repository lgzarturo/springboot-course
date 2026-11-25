package com.lgzarturo.springbootcourse.config.security

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS3 (H1): rate limiting en endpoints de autenticación.
 * Corre en contexto propio con límites pequeños y filtro habilitado.
 */
@SpringBootTest(
    properties = [
        "auth.rate-limit.enabled=true",
        "auth.rate-limit.per-minute=3",
    ],
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Rate Limit Integration Tests")
class RateLimitIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("El 4.º login debe retornar 429 con Retry-After")
    fun `fourth login returns too many requests`() {
        repeat(3) {
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ratelimit.login@pokemon.com")),
                ).andExpect(status().isUnauthorized)
        }

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("ratelimit.login@pokemon.com")),
            ).andExpect(status().isTooManyRequests)
            .andExpect(header().exists("Retry-After"))
    }

    @Test
    @DisplayName("El 4.º registro debe retornar 429")
    fun `fourth registration returns too many requests`() {
        repeat(3) { index ->
            mockMvc
                .perform(
                    post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("ratelimit.reg$index@pokemon.com")),
                ).andExpect(status().isCreated)
        }

        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("ratelimit.reg3@pokemon.com")),
            ).andExpect(status().isTooManyRequests)
    }

    @Test
    @DisplayName("Endpoints fuera de auth no deben limitarse")
    fun `non auth endpoints are not rate limited`() {
        repeat(7) {
            mockMvc
                .perform(get("/api/v1/hotels"))
                .andExpect(status().isUnauthorized)
        }
    }

    private fun loginBody(email: String): String = """{"email":"$email","password":"WrongPass1"}"""

    private fun registerBody(email: String): String =
        """{"email":"$email","password":"Pikachu123!","firstName":"Test","lastName":"Trainer"}"""
}
