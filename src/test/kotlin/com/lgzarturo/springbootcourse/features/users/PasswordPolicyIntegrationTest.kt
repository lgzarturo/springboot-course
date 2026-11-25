package com.lgzarturo.springbootcourse.features.users

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
 * TDD Tests for WS4 (H5): política de longitud de contraseñas.
 * bcrypt trunca a 72 bytes: más de 72 debe rechazarse con 400.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Password Policy Integration Tests")
class PasswordPolicyIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("Registro con contraseña de 73 caracteres debe retornar 400")
    fun `registration with 73 char password returns bad request`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("policy.long@pokemon.com", "Aa1!" + "x".repeat(69))),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Registro con contraseña de 72 caracteres debe retornar 201")
    fun `registration with 72 char password returns created`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("policy.max@pokemon.com", "Aa1!" + "x".repeat(68))),
            ).andExpect(status().isCreated)
    }

    @Test
    @DisplayName("Registro con contraseña corta debe retornar 400")
    fun `registration with short password returns bad request`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("policy.short@pokemon.com", "Aa1!xyz")),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("Registro con contraseña en blanco debe retornar 400")
    fun `registration with blank password returns bad request`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("policy.blank@pokemon.com", "")),
            ).andExpect(status().isBadRequest)
    }

    private fun registerBody(
        email: String,
        password: String,
    ): String =
        """{"email":"$email","password":"$password","firstName":"Test","lastName":"Trainer"}"""
}
