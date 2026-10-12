package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import com.lgzarturo.springbootcourse.support.LayeredTestSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

/**
 * Pruebas E2E para la gestión de usuarios (BC-009).
 * Ejecuta el servidor HTTP completo con puerto aleatorio y valida el ciclo de vida del usuario.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("User E2E Tests")
class UserE2ETest {
    @LocalServerPort
    private var port: Int = 0

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    private val restTemplate = TestRestTemplate()

    private fun baseUrl(path: String): String = "http://localhost:$port$path"

    @Test
    @DisplayName("debe_registrar_y_autenticar_usuario_en_flujo_e2e")
    fun `debe_registrar_y_autenticar_usuario_en_flujo_e2e`() {
        val email = LayeredTestSupport.uniqueEmail("e2e.register")
        val registerRequest =
            CreateUserRequest(
                email = email,
                password = "Password123!",
                firstName = "Red",
                lastName = "Champion",
                phoneNumber = "+14155551111",
            )

        val registerHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val registerEntity = HttpEntity(registerRequest, registerHeaders)

        val registerResponse =
            restTemplate.postForEntity(
                baseUrl("/api/v1/users"),
                registerEntity,
                UserResponse::class.java,
            )

        assertEquals(HttpStatus.CREATED, registerResponse.statusCode)
        assertNotNull(registerResponse.body)
        assertEquals(email, registerResponse.body?.email)

        // Login E2E
        val loginBody = mapOf("email" to email, "password" to "Password123!")
        val loginResponse =
            restTemplate.postForEntity(
                baseUrl("/api/v1/auth/login"),
                HttpEntity(loginBody, registerHeaders),
                Map::class.java,
            )

        assertEquals(HttpStatus.OK, loginResponse.statusCode)
        assertNotNull(loginResponse.body?.get("token"))
    }

    @Test
    @DisplayName("debe_consultar_perfil_con_token_jwt_en_flujo_e2e")
    fun `debe_consultar_perfil_con_token_jwt_en_flujo_e2e`() {
        val email = LayeredTestSupport.uniqueEmail("e2e.profile")
        val registerRequest =
            CreateUserRequest(
                email = email,
                password = "Password123!",
                firstName = "Blue",
                lastName = "Rival",
                phoneNumber = null,
            )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val created =
            restTemplate.postForEntity(
                baseUrl("/api/v1/users"),
                HttpEntity(registerRequest, headers),
                UserResponse::class.java,
            )

        assertEquals(HttpStatus.CREATED, created.statusCode)
        val userId = requireNotNull(created.body?.id)

        val token = LayeredTestSupport.mintTokenForRole(jwtSecret, email, UserRole.GUEST)
        val authHeaders = HttpHeaders().apply { setBearerAuth(token) }

        val profileResponse =
            restTemplate.exchange(
                baseUrl("/api/v1/users/$userId"),
                HttpMethod.GET,
                HttpEntity<Void>(authHeaders),
                UserResponse::class.java,
            )

        assertEquals(HttpStatus.OK, profileResponse.statusCode)
        assertEquals("Blue", profileResponse.body?.firstName)
    }

    @Test
    @DisplayName("debe_rechazar_registro_con_correo_duplicado_en_flujo_e2e")
    fun `debe_rechazar_registro_con_correo_duplicado_en_flujo_e2e`() {
        val email = LayeredTestSupport.uniqueEmail("e2e.dup")
        val registerRequest =
            CreateUserRequest(
                email = email,
                password = "Password123!",
                firstName = "Oak",
                lastName = "Professor",
            )

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val first =
            restTemplate.postForEntity(
                baseUrl("/api/v1/users"),
                HttpEntity(registerRequest, headers),
                UserResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, first.statusCode)

        val second =
            restTemplate.postForEntity(
                baseUrl("/api/v1/users"),
                HttpEntity(registerRequest, headers),
                String::class.java,
            )
        assertEquals(HttpStatus.CONFLICT, second.statusCode)
    }
}
