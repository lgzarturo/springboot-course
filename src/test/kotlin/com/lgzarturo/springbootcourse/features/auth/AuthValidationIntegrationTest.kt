package com.lgzarturo.springbootcourse.features.auth

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS4 (H5): validación del login.
 * Contraseñas gigantes no deben llegar a bcrypt (DoS).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Auth Validation Integration Tests")
class AuthValidationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("Login con contraseña de 100 caracteres debe retornar 400")
    fun `login with oversized password returns bad request`() {
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("validation.big@pokemon.com", "Aa1!" + "y".repeat(96))),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Login con contraseña en blanco debe retornar 400")
    fun `login with blank password returns bad request`() {
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("validation.blank@pokemon.com", "")),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Login con usuario inexistente debe retornar 401 genérico")
    fun `login with unknown user returns unauthorized`() {
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("validation.ghost@pokemon.com", "Password123!")),
            ).andExpect(status().isUnauthorized)
    }

    private fun loginBody(
        email: String,
        password: String,
    ): String = """{"email":"$email","password":"$password"}"""
}
