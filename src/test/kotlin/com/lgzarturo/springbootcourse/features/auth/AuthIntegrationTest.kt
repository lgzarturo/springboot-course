package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.features.users.UserRepository
import com.lgzarturo.springbootcourse.security.JwtTestHelper
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.Date

/**
 * Pruebas de integración MockMvc para la autenticación JWT (BC-007).
 * Cubre solicitudes públicas, autenticadas, prohibidas y con tokens expirados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests")
class AuthIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.issuer:springboot-course-test}")
    private lateinit var jwtIssuer: String

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 200 OK con un JWT firmado")
    fun `login with valid credentials returns signed JWT`() {
        register("ash.login@pokemon.com")

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("ash.login@pokemon.com", "Pikachu123!")),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
    }

    @Test
    @DisplayName("POST /api/v1/users debe seguir público y crear usuarios GUEST")
    fun `registration stays public and creates GUEST users`() {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody("misty.register@pokemon.com")),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.role").value("GUEST"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 401 con credenciales inválidas")
    fun `login with invalid credentials returns unauthorized`() {
        register("brock.login@pokemon.com")

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("brock.login@pokemon.com", "WrongPass1")),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 401 para un usuario desactivado")
    fun `login with deactivated user returns unauthorized`() {
        register("inactive@pokemon.com")
        val user = requireNotNull(userRepository.findByEmail("inactive@pokemon.com"))
        userRepository.save(user.deactivate())

        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody("inactive@pokemon.com", "Pikachu123!")),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("GET /api/v1/hotels sin token debe retornar 401")
    fun `protected endpoint without token returns unauthorized`() {
        mockMvc
            .perform(get("/api/v1/hotels"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Un token GUEST debe leer hoteles pero no crearlos")
    fun `guest token reads hotels but cannot create them`() {
        register("guest.hotels@pokemon.com")
        val token = loginToken("guest.hotels@pokemon.com", "Pikachu123!")

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Guest Hotel","address":"Nowhere"}"""),
            ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("Un token STAFF debe crear hoteles mientras anónimo recibe 401")
    fun `staff token creates hotels while anonymous is rejected`() {
        val staffToken =
            JwtTestHelper.mintToken(
                jwtSecret,
                "staff@pokemon.com",
                listOf("ROLE_STAFF"),
                jwtIssuer,
                60_000,
            )

        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .header("Authorization", "Bearer $staffToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Staff Hotel","address":"Somewhere"}"""),
            ).andExpect(status().isCreated)

        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Anon Hotel","address":"Nowhere"}"""),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Un token expirado debe retornar 401")
    fun `expired token returns unauthorized`() {
        val expiredToken =
            JwtTestHelper.mintToken(jwtSecret, "ash@pokemon.com", listOf("ROLE_GUEST"), jwtIssuer, -60_000)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $expiredToken"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Un token mal firmado o malformado debe retornar 401")
    fun `invalidly signed or malformed token returns unauthorized`() {
        val wrongSignatureToken =
            JwtTestHelper.mintToken(
                "AnotherSecretKeyForJwtSigningThatIsLongEnough0987654321",
                "ash@pokemon.com",
                listOf("ROLE_GUEST"),
                jwtIssuer,
                60_000,
            )

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $wrongSignatureToken"),
            ).andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer not-a-jwt"),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("El JWT emitido debe incluir usuario, roles y expiración")
    fun `login token claims include username roles and expiration`() {
        register("claims@pokemon.com")
        val token = loginToken("claims@pokemon.com", "Pikachu123!")

        val claims = JwtTestHelper.parseClaims(token, jwtSecret)
        org.junit.jupiter.api.Assertions
            .assertEquals("claims@pokemon.com", claims.subject)
        assertTrue(claims.get("roles", List::class.java).contains("ROLE_GUEST"))
        assertTrue(requireNotNull(claims.expiration).after(Date()))
    }

    private fun register(email: String) {
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerBody(email)),
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
                        .content(loginBody(email, password)),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
        return objectMapper.readTree(body).get("token").asText()
    }

    private fun registerBody(email: String): String =
        """{"email":"$email","password":"Pikachu123!","firstName":"Test","lastName":"Trainer"}"""

    private fun loginBody(
        email: String,
        password: String,
    ): String = """{"email":"$email","password":"$password"}"""
}
