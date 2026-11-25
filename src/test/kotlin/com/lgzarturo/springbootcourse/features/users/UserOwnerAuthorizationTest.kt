package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.auth.AuthService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * TDD Tests for WS2: autorización a nivel de objeto en GET /api/v1/users/{id} (H2).
 * Solo el dueño del perfil o un admin pueden leerlo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("User Owner Authorization Tests")
class UserOwnerAuthorizationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Test
    @DisplayName("Un usuario puede leer su propio perfil")
    fun `user can read own profile`() {
        val owner = createUser("owner.self@pokemon.com", UserRole.GUEST)
        val token = authService.authenticate("owner.self@pokemon.com", "Password123!")

        mockMvc
            .perform(
                get("/api/v1/users/${owner.id.value}")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
    }

    @Test
    @DisplayName("Un usuario no puede leer el perfil de otro usuario")
    fun `user cannot read another profile`() {
        createUser("owner.a@pokemon.com", UserRole.GUEST)
        val other = createUser("owner.b@pokemon.com", UserRole.GUEST)
        val token = authService.authenticate("owner.a@pokemon.com", "Password123!")

        mockMvc
            .perform(
                get("/api/v1/users/${other.id.value}")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("Un admin puede leer cualquier perfil")
    fun `admin can read any profile`() {
        val target = createUser("owner.target@pokemon.com", UserRole.GUEST)
        createUser("owner.admin@pokemon.com", UserRole.ADMIN)
        val adminToken = authService.authenticate("owner.admin@pokemon.com", "Password123!")

        mockMvc
            .perform(
                get("/api/v1/users/${target.id.value}")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isOk)
    }

    private fun createUser(
        email: String,
        role: UserRole,
    ): User =
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Test",
                lastName = "Trainer",
                phoneNumber = null,
                role = role,
            ),
        )
}
