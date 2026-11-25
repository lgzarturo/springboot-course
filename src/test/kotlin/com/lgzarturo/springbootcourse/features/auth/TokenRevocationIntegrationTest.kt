package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.features.users.UpdateUserUseCase
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import com.lgzarturo.springbootcourse.security.JwtTestHelper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
 * TDD Tests for WS1: revocación masiva por usuario.
 * Desactivar un usuario debe invalidar sus tokens ya emitidos (H3).
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Token Revocation Integration Tests")
class TokenRevocationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserDomainRepository

    @Autowired
    private lateinit var updateUserUseCase: UpdateUserUseCase

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.issuer:springboot-course-test}")
    private lateinit var jwtIssuer: String

    @Test
    @DisplayName("Desactivar un usuario debe invalidar su token ya emitido")
    fun `deactivated user token is rejected`() {
        register("revoke.deact@pokemon.com")
        val token = loginToken("revoke.deact@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)

        val user = requireNotNull(userRepository.findByEmail("revoke.deact@pokemon.com"))
        updateUserUseCase.deactivate(user.id.value)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Un token de un usuario inexistente debe retornar 401")
    fun `token for unknown user is rejected`() {
        val orphanToken =
            JwtTestHelper.mintToken(jwtSecret, "999999", listOf("ROLE_GUEST", "hotels:read"), jwtIssuer, 60_000)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $orphanToken"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Un token sin claim de roles debe retornar 401 y no 500 (M4)")
    fun `token without roles claim returns unauthorized not server error`() {
        val roleslessToken = JwtTestHelper.mintTokenWithoutRoles(jwtSecret, "1", jwtIssuer, 60_000)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $roleslessToken"),
            ).andExpect(status().isUnauthorized)
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
