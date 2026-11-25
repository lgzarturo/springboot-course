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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS4 (M6): normalización de emails a minúsculas.
 * "User@x.com" y "user@x.com" son la misma cuenta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Email Normalization Integration Tests")
class EmailNormalizationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @DisplayName("Login con distinta capitalización debe funcionar")
    fun `login is case insensitive on email`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("Case.Mix.Norm@pokemon.com")),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value("case.mix.norm@pokemon.com"))

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("case.mix.norm@pokemon.com")),
            ).andExpect(status().isOk)
    }

    @Test
    @DisplayName("Registro duplicado con distinta capitalización debe retornar 409")
    fun `duplicate registration with different case returns conflict`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("dup.case.norm@pokemon.com")),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("DUP.CASE.NORM@pokemon.com")),
            ).andExpect(status().isConflict)
    }

    private fun registerBody(email: String): String =
        """{"email":"$email","password":"Pikachu123!","firstName":"Test","lastName":"Trainer"}"""

    private fun loginBody(email: String): String = """{"email":"$email","password":"Pikachu123!"}"""
}
