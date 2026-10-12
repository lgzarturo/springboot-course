package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.CreateUserRequest
import com.lgzarturo.springbootcourse.features.users.dto.UpdateUserRequest
import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.support.LayeredTestSupport
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * Pruebas de integración MockMvc full-context para la feature de usuarios (BC-009).
 * Valida la persistencia real, validaciones y reglas de autorización bajo Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("User Layered Integration Tests")
class UserIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Test
    @DisplayName("debe_registrar_usuario_cuando_datos_son_validos")
    fun `debe_registrar_usuario_cuando_datos_son_validos`() {
        val email = LayeredTestSupport.uniqueEmail("ash.register")
        val request =
            CreateUserRequest(
                email = email,
                password = "Password123!",
                firstName = "Ash",
                lastName = "Ketchum",
                phoneNumber = "+14155552671",
            )

        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("GUEST"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    @DisplayName("debe_retornar_conflicto_cuando_correo_ya_existe")
    fun `debe_retornar_conflicto_cuando_correo_ya_existe`() {
        val email = LayeredTestSupport.uniqueEmail("duplicate")
        val request =
            CreateUserRequest(
                email = email,
                password = "Password123!",
                firstName = "Brock",
                lastName = "Boulder",
                phoneNumber = null,
            )

        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)

        // Segundo intento con el mismo email
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isConflict)
    }

    @Test
    @DisplayName("debe_obtener_usuario_por_id_cuando_solicitante_esta_autenticado")
    fun `debe_obtener_usuario_por_id_cuando_solicitante_esta_autenticado`() {
        val email = LayeredTestSupport.uniqueEmail("misty.profile")
        val user =
            userRepository.save(
                User.create(
                    email = Email(email),
                    password = Password("Password123!"),
                    firstName = "Misty",
                    lastName = "Waterflower",
                    phoneNumber = null,
                    role = UserRole.GUEST,
                ),
            )

        val token = LayeredTestSupport.mintTokenForRole(jwtSecret, email, UserRole.GUEST)

        mockMvc
            .perform(
                get("/api/v1/users/${user.id.value}")
                    .header("Authorization", "Bearer $token"),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(user.id.value))
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.firstName").value("Misty"))
    }

    @Test
    @DisplayName("debe_actualizar_perfil_cuando_datos_son_validos_y_usuario_es_admin")
    fun `debe_actualizar_perfil_cuando_datos_son_validos_y_usuario_es_admin`() {
        val email = LayeredTestSupport.uniqueEmail("update.target")
        val user =
            userRepository.save(
                User.create(
                    email = Email(email),
                    password = Password("Password123!"),
                    firstName = "Gary",
                    lastName = "Oak",
                    phoneNumber = null,
                    role = UserRole.GUEST,
                ),
            )

        val adminEmail = LayeredTestSupport.uniqueEmail("admin.actor")
        val adminToken = LayeredTestSupport.mintTokenForRole(jwtSecret, adminEmail, UserRole.ADMIN)

        val updateRequest =
            UpdateUserRequest(
                firstName = "Blue",
                lastName = "Oak",
                phoneNumber = "+14155559999",
            )

        mockMvc
            .perform(
                put("/api/v1/users/${user.id.value}")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.firstName").value("Blue"))
            .andExpect(jsonPath("$.phoneNumber").value("+14155559999"))
    }

    @Test
    @DisplayName("debe_retornar_no_encontrado_cuando_id_no_existe")
    fun `debe_retornar_no_encontrado_cuando_id_no_existe`() {
        val adminEmail = LayeredTestSupport.uniqueEmail("admin.check")
        val adminToken = LayeredTestSupport.mintTokenForRole(jwtSecret, adminEmail, UserRole.ADMIN)

        mockMvc
            .perform(
                get("/api/v1/users/999999")
                    .header("Authorization", "Bearer $adminToken"),
            ).andExpect(status().isNotFound)
    }
}
