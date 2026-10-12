package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.auth.AuthService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for BC-008: Add roles and granular authorities.
 * Covers role and authority mapping, security context loading, and endpoint authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("User Role and Authority Authorization Tests")
class UserRoleAuthorizationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.issuer:springboot-course-test}")
    private lateinit var jwtIssuer: String

    @Test
    @DisplayName("FR-001 & FR-002: Login token must contain roles and granular authorities")
    fun `login token contains roles and granular authorities`() {
        val email = "trainer.auth@pokemon.com"
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Red",
                lastName = "Trainer",
                phoneNumber = null,
                role = UserRole.GUEST,
            ),
        )

        val token = authService.authenticate(email, "Password123!")
        val claims = JwtTestHelper.parseClaims(token, jwtSecret)

        @Suppress("UNCHECKED_CAST")
        val roles = claims.get("roles", List::class.java) as List<String>

        assertTrue(roles.contains("ROLE_GUEST"), "Should contain ROLE_GUEST")
        assertTrue(roles.contains("hotels:read"), "Should contain hotels:read authority")
        assertTrue(roles.contains("reservations:create_own"), "Should contain reservations:create_own authority")
    }

    @Test
    @DisplayName("FR-003: GUEST can read hotels but cannot create or modify them")
    fun `guest can read hotels but cannot create them`() {
        val guestToken =
            JwtTestHelper.mintToken(
                jwtSecret,
                "guest@pokemon.com",
                listOf("ROLE_GUEST", "hotels:read", "reservations:create_own"),
                jwtIssuer,
                60_000,
            )

        mockMvc
            .perform(
                get("/api/v1/hotels")
                    .header("Authorization", "Bearer $guestToken"),
            ).andExpect(status().isOk)

        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .header("Authorization", "Bearer $guestToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Guest Created Hotel","address":"Cerulean City"}"""),
            ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("FR-004: STAFF has hotel and reservation management authorities")
    fun `staff has hotel management authorities`() {
        val staffToken =
            JwtTestHelper.mintToken(
                jwtSecret,
                "staff@pokemon.com",
                listOf("ROLE_STAFF", "hotels:read", "hotels:manage", "reservations:manage"),
                jwtIssuer,
                60_000,
            )

        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .header("Authorization", "Bearer $staffToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"Staff Created Hotel","address":"Vermilion City"}"""),
            ).andExpect(status().isCreated)
    }

    @Test
    @DisplayName("FR-004 & FR-006: ADMIN has full access to user management operations")
    fun `admin has access to user management operations`() {
        val adminToken =
            JwtTestHelper.mintToken(
                jwtSecret,
                "admin@pokemon.com",
                listOf("ROLE_ADMIN", "users:read", "users:write", "hotels:manage", "reservations:manage"),
                jwtIssuer,
                60_000,
            )

        val guestToken =
            JwtTestHelper.mintToken(
                jwtSecret,
                "guest2@pokemon.com",
                listOf("ROLE_GUEST", "hotels:read"),
                jwtIssuer,
                60_000,
            )

        // Guest attempting to list all users is forbidden
        mockMvc
            .perform(
                get("/api/v1/users")
                    .header("Authorization", "Bearer $guestToken"),
            ).andExpect(status().isForbidden)

        // Admin can list all users
        mockMvc
            .perform(
                get("/api/v1/users")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
    }
}
