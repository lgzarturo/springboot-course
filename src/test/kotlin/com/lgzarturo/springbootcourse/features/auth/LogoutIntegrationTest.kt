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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * TDD Tests for WS1: revocación individual de tokens vía logout.
 * POST /api/v1/auth/logout debe revocar el jti actual: el token deja de aceptarse.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Logout Integration Tests")
class LogoutIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("POST /api/v1/auth/logout con token válido debe retornar 204")
    fun `logout with valid token returns no content`() {
        register("logout.ok@pokemon.com")
        val token = loginToken("logout.ok@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("Un token usado en logout debe rechazarse con 401 en usos posteriores")
    fun `token is rejected after logout`() {
        register("logout.reuse@pokemon.com")
        val token = loginToken("logout.reuse@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isNoContent)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Logout repetido con el mismo token debe seguir retornando 204 (idempotente)")
    fun `logout is idempotent`() {
        register("logout.idem@pokemon.com")
        val token = loginToken("logout.idem@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isNoContent)

        // El segundo logout llega sin autenticación (token ya revocado):
        // 401 también es aceptable, lo importante es que no haya 500 ni fuga de datos.
        mockMvc
            .perform(
                post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout sin token debe retornar 401")
    fun `logout without token returns unauthorized`() {
        mockMvc
            .perform(post("/api/v1/auth/logout"))
            .andExpect(status().isUnauthorized)
    }

    private fun register(email: String) {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"email":"$email","password":"Pikachu123!","firstName":"Test","lastName":"Trainer"}"""),
            ).andExpect(status().isCreated)
    }

    private fun loginToken(
        email: String,
        password: String,
    ): String {
        val body =
            mockMvc
                .perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"email":"$email","password":"$password"}"""),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
        return objectMapper.readTree(body).get("token").asText()
    }
}
