package com.lgzarturo.springbootcourse.config.security

import com.lgzarturo.springbootcourse.features.users.UserRole
import com.lgzarturo.springbootcourse.security.JwtTestHelper
import com.lgzarturo.springbootcourse.security.WithMockDomainUser
import com.lgzarturo.springbootcourse.security.WithMockUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

/**
 * Suite de verificación completa para el comportamiento de endpoints protegidos (BC-010).
 * Cubre autenticación, autorización basada en roles y autoridades, invalidación de tokens
 * y soporte de fixtures aislados para pruebas.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Protected Endpoint Authorization Matrix Tests")
class ProtectedEndpointAuthorizationTest {
    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var mockMvc: MockMvc

    @Value("\${jwt.secret:TestSecretKeyForJwtSigningThatIsLongEnough1234567890}")
    private lateinit var jwtSecret: String

    @Value("\${jwt.issuer:springboot-course-test}")
    private lateinit var jwtIssuer: String

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    @DisplayName("debe_retornar_401_cuando_solicitud_no_tiene_autenticacion")
    fun `debe_retornar_401_cuando_solicitud_no_tiene_autenticacion`() {
        mockMvc.perform(get("/api/v1/hotels"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Unauthorized Hotel","address":"Route 1"}"""),
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("debe_retornar_403_cuando_solicitud_tiene_roles_insuficientes_con_jwt")
    fun `debe_retornar_403_cuando_solicitud_tiene_roles_insuficientes_con_jwt`() {
        val guestToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "guest@pokemon.com",
            roles = UserRole.GUEST.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        val staffToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "staff@pokemon.com",
            roles = UserRole.STAFF.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        // GUEST no tiene permiso para crear hoteles
        mockMvc.perform(
            post("/api/v1/hotels")
                .header("Authorization", "Bearer $guestToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Forbidden Hotel","address":"Lavender Town"}"""),
        ).andExpect(status().isForbidden)

        // GUEST no tiene permiso para listar usuarios
        mockMvc.perform(
            get("/api/v1/users")
                .header("Authorization", "Bearer $guestToken"),
        ).andExpect(status().isForbidden)

        // STAFF no tiene permiso para listar usuarios
        mockMvc.perform(
            get("/api/v1/users")
                .header("Authorization", "Bearer $staffToken"),
        ).andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("debe_autorizar_exitosamente_cuando_credenciales_jwt_son_validas_para_roles_permitidos")
    fun `debe_autorizar_exitosamente_cuando_credenciales_jwt_son_validas_para_roles_permitidos`() {
        val guestToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "guest.ok@pokemon.com",
            roles = UserRole.GUEST.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        val staffToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "staff.ok@pokemon.com",
            roles = UserRole.STAFF.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        val adminToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "admin.ok@pokemon.com",
            roles = UserRole.ADMIN.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        // GUEST puede leer hoteles
        mockMvc.perform(
            get("/api/v1/hotels")
                .header("Authorization", "Bearer $guestToken"),
        ).andExpect(status().isOk)

        // STAFF puede crear hoteles
        mockMvc.perform(
            post("/api/v1/hotels")
                .header("Authorization", "Bearer $staffToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Staff Authorized Hotel","address":"Celadon City"}"""),
        ).andExpect(status().isCreated)

        // ADMIN puede leer lista de usuarios
        mockMvc.perform(
            get("/api/v1/users")
                .header("Authorization", "Bearer $adminToken"),
        ).andExpect(status().isOk)
    }

    @Test
    @DisplayName("debe_retornar_401_cuando_token_es_expirado_o_malformado")
    fun `debe_retornar_401_cuando_token_es_expirado_o_malformado`() {
        val expiredToken = JwtTestHelper.mintToken(
            secret = jwtSecret,
            subject = "expired@pokemon.com",
            roles = UserRole.ADMIN.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = -10_000,
        )

        val badSecretToken = JwtTestHelper.mintToken(
            secret = "WrongSecretKeyForJwtSigningThatIsLongEnough1234567890",
            subject = "forged@pokemon.com",
            roles = UserRole.ADMIN.allGrantedAuthorities(),
            issuer = jwtIssuer,
            ttlMillis = 60_000,
        )

        // Token expirado
        mockMvc.perform(
            get("/api/v1/hotels")
                .header("Authorization", "Bearer $expiredToken"),
        ).andExpect(status().isUnauthorized)

        // Token firmado con llave incorrecta
        mockMvc.perform(
            get("/api/v1/hotels")
                .header("Authorization", "Bearer $badSecretToken"),
        ).andExpect(status().isUnauthorized)

        // Token malformado
        mockMvc.perform(
            get("/api/v1/hotels")
                .header("Authorization", "Bearer invalid.malformed.token"),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockDomainUser(role = UserRole.GUEST)
    @DisplayName("debe_permitir_lectura_hoteles_cuando_se_usa_mock_domain_user_guest")
    fun `debe_permitir_lectura_hoteles_cuando_se_usa_mock_domain_user_guest`() {
        mockMvc.perform(get("/api/v1/hotels"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockDomainUser(role = UserRole.GUEST)
    @DisplayName("debe_denegar_creacion_hoteles_cuando_se_usa_mock_domain_user_guest")
    fun `debe_denegar_creacion_hoteles_cuando_se_usa_mock_domain_user_guest`() {
        mockMvc.perform(
            post("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Guest Attempt","address":"Pallet Town"}"""),
        ).andExpect(status().isForbidden)
    }

    @Test
    @WithMockDomainUser(role = UserRole.STAFF)
    @DisplayName("debe_permitir_creacion_hoteles_cuando_se_usa_mock_domain_user_staff")
    fun `debe_permitir_creacion_hoteles_cuando_se_usa_mock_domain_user_staff`() {
        mockMvc.perform(
            post("/api/v1/hotels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Staff Created Hotel","address":"Cinnabar Island"}"""),
        ).andExpect(status().isCreated)
    }

    @Test
    @WithMockDomainUser(role = UserRole.ADMIN)
    @DisplayName("debe_permitir_gestion_usuarios_cuando_se_usa_mock_domain_user_admin")
    fun `debe_permitir_gestion_usuarios_cuando_se_usa_mock_domain_user_admin`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
    }

    @Test
    @WithMockUser(username = "custom.admin@pokemon.com", authorities = ["ROLE_ADMIN", "users:read"])
    @DisplayName("debe_permitir_acceso_con_mock_user_personalizado_y_autoridades")
    fun `debe_permitir_acceso_con_mock_user_personalizado_y_autoridades`() {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isOk)
    }
}
