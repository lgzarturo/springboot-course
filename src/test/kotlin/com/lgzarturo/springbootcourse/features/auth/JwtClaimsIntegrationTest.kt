package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import com.lgzarturo.springbootcourse.security.JwtTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * TDD Tests for WS0: nuevo contrato de claims JWT.
 * sub=userId, claims email/roles/jti/v.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("JWT Claims Integration Tests")
class JwtClaimsIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserDomainRepository

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Test
    @DisplayName("El JWT emitido debe usar sub=userId con claims email, jti y v")
    fun `login token uses new claims contract`() {
        register("claims.new@pokemon.com")
        val token = loginToken("claims.new@pokemon.com", "Pikachu123!")

        val user = requireNotNull(userRepository.findByEmail("claims.new@pokemon.com"))
        val claims = JwtTestHelper.parseClaims(token, jwtSecret)

        assertEquals(user.id.value.toString(), claims.subject)
        assertEquals("claims.new@pokemon.com", claims.get("email", String::class.java))
        assertNotNull(claims.id, "jti must be present")
        assertEquals(0, (claims.get("v", Number::class.java) ?: -1).toInt())
        assertTrue(claims.get("roles", List::class.java).contains("ROLE_GUEST"))
    }

    @Test
    @DisplayName("Cada emisión debe producir un jti distinto")
    fun `each login mints a unique jti`() {
        register("claims.jti@pokemon.com")
        val first = loginToken("claims.jti@pokemon.com", "Pikachu123!")
        val second = loginToken("claims.jti@pokemon.com", "Pikachu123!")

        val firstJti = JwtTestHelper.parseClaims(first, jwtSecret).id
        val secondJti = JwtTestHelper.parseClaims(second, jwtSecret).id

        assertNotNull(firstJti)
        assertNotNull(secondJti)
        org.junit.jupiter.api.Assertions
            .assertNotEquals(firstJti, secondJti)
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
