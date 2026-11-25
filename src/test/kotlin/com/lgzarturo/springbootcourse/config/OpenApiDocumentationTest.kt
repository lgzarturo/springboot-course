package com.lgzarturo.springbootcourse.config

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

/**
 * Pruebas de contrato OpenAPI (BC-012).
 * Verifican que `/api-docs` documente todos los endpoints, esquemas DTO,
 * seguridad JWT y respuestas de error de la API.
 */
@SpringBootTest(
    properties = [
        // El application.yaml de tests oculta al principal: replicar la ruta de producción aquí.
        "springdoc.api-docs.path=/api-docs",
    ],
)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("OpenAPI Documentation Contract Tests")
class OpenApiDocumentationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("debe_retornar_documento_openapi_cuando_se_consulta_api_docs")
    fun `debe_retornar_documento_openapi_cuando_se_consulta_api_docs`() {
        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
            .andExpect(jsonPath("$.info.title").isNotEmpty)
            .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/auth/logout']['post']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users']['post']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users/{id}']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users/{id}']['put']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users/{id}']['delete']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels']['post']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels/{id}']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels/{id}']['put']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels/{id}']['delete']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples']['post']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples/{id}']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples/{id}']['put']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples/{id}']['delete']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/examples/{id}']['patch']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/ping']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/ping/{message}']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/ping/health']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/sentry/event']['get']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/sentry/message']['get']").exists())
    }

    @Test
    @DisplayName("debe_agrupar_endpoints_por_feature_cuando_se_consulta_api_docs")
    fun `debe_agrupar_endpoints_por_feature_cuando_se_consulta_api_docs`() {
        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.tags[?(@.name == 'Auth')]").isNotEmpty)
            .andExpect(jsonPath("$.tags[?(@.name == 'Users')]").isNotEmpty)
            .andExpect(jsonPath("$.tags[?(@.name == 'Hotels')]").isNotEmpty)
            .andExpect(jsonPath("$.tags[?(@.name == 'Example')]").isNotEmpty)
            .andExpect(jsonPath("$.tags[?(@.name == 'Ping')]").isNotEmpty)
            .andExpect(jsonPath("$.tags[?(@.name == 'Sentry')]").isNotEmpty)
            .andExpect(jsonPath("$['paths']['/api/v1/users']['post']['tags'][0]").value("Users"))
            .andExpect(jsonPath("$['paths']['/api/v1/hotels']['get']['tags'][0]").value("Hotels"))
            .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']['tags'][0]").value("Auth"))
            .andExpect(jsonPath("$['paths']['/api/v1/auth/logout']['post']['tags'][0]").value("Auth"))
    }

    @Test
    @DisplayName("debe_documentar_operaciones_cuando_se_consulta_cada_endpoint")
    fun `debe_documentar_operaciones_cuando_se_consulta_cada_endpoint`() {
        val content =
            mockMvc
                .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .contentAsString
        val paths = objectMapper.readTree(content).path("paths")
        val methods = setOf("get", "post", "put", "delete", "patch")
        assertTrue(paths.size() > 0, "El documento OpenAPI debe declarar paths")
        paths.properties().forEach { (path, item) ->
            item.properties().forEach { (method, operation) ->
                if (method in methods) {
                    assertTrue(
                        operation.path("summary").asText().isNotBlank(),
                        "La operación $method $path debe tener summary",
                    )
                    assertTrue(
                        operation.path("description").asText().isNotBlank(),
                        "La operación $method $path debe tener description",
                    )
                    assertTrue(
                        operation.path("responses").size() > 0,
                        "La operación $method $path debe declarar responses",
                    )
                }
            }
        }

        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$['paths']['/api/v1/users']['post']['responses']['201']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users']['post']['responses']['400']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/users']['post']['responses']['409']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels']['post']['responses']['201']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels/{id}']['get']['responses']['200']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/hotels/{id}']['get']['responses']['404']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']['responses']['200']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/auth/login']['post']['responses']['401']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/auth/logout']['post']['responses']['204']").exists())
            .andExpect(jsonPath("$['paths']['/api/v1/auth/logout']['post']['responses']['401']").exists())
    }

    @Test
    @DisplayName("debe_exponer_restricciones_y_ejemplos_cuando_se_consultan_esquemas_dto")
    fun `debe_exponer_restricciones_y_ejemplos_cuando_se_consultan_esquemas_dto`() {
        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$['components']['schemas']['CreateUserRequest']").exists())
            .andExpect(
                jsonPath(
                    "$['components']['schemas']['CreateUserRequest']['properties']['email']['format']",
                ).value("email"),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['CreateUserRequest']['properties']['email']['example']",
                ).exists(),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['CreateUserRequest']['properties']['password']['pattern']",
                ).exists(),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['CreateHotelRequest']['properties']['name']['maxLength']",
                ).value(255),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['CreateHotelRequest']['properties']['name']['example']",
                ).exists(),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['LoginRequest']['properties']['email']['example']",
                ).exists(),
            ).andExpect(
                jsonPath(
                    "$['components']['schemas']['HotelResponse']['properties']['name']['example']",
                ).exists(),
            )
    }

    @Test
    @DisplayName("debe_exponer_control_authorize_cuando_se_configura_jwt")
    fun `debe_exponer_control_authorize_cuando_se_configura_jwt`() {
        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$['components']['securitySchemes']['bearerAuth']['type']").value("http"),
            ).andExpect(
                jsonPath("$['components']['securitySchemes']['bearerAuth']['scheme']").value("bearer"),
            ).andExpect(
                jsonPath("$['components']['securitySchemes']['bearerAuth']['bearerFormat']").value("JWT"),
            ).andExpect(
                jsonPath("$['paths']['/api/v1/users/{id}']['get']['security']").isNotEmpty,
            ).andExpect(
                jsonPath("$['paths']['/api/v1/auth/login']['post']['security']").doesNotExist(),
            )
    }

    @Test
    @DisplayName("debe_documentar_errores_con_esquema_cuando_respuesta_es_error")
    fun `debe_documentar_errores_con_esquema_cuando_respuesta_es_error`() {
        mockMvc
            .perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$['components']['schemas']['ErrorResponse']").exists())
            .andExpect(
                jsonPath("$['components']['schemas']['ErrorResponse']['properties']['status']").exists(),
            ).andExpect(
                jsonPath("$['components']['schemas']['ErrorResponse']['properties']['error']").exists(),
            ).andExpect(
                jsonPath("$['components']['schemas']['ErrorResponse']['properties']['message']").exists(),
            ).andExpect(
                jsonPath("$['components']['schemas']['ErrorResponse']['properties']['path']").exists(),
            ).andExpect(
                jsonPath(
                    "$['paths']['/api/v1/users']['post']['responses']['400']" +
                        "['content']['application/json']['schema']['\$ref']",
                ).value("#/components/schemas/ErrorResponse"),
            )
    }

    @Test
    @DisplayName("debe_servir_swagger_ui_cuando_se_solicita")
    fun `debe_servir_swagger_ui_cuando_se_solicita`() {
        val response =
            mockMvc
                .perform(get("/swagger-ui.html"))
                .andReturn()
                .response
        assertTrue(
            response.status in setOf(200, 301, 302, 304),
            "Swagger UI debe responder 200 o redirección, fue ${response.status}",
        )
        if (response.status == 200) {
            assertEquals("text/html", response.contentType?.substringBefore(";"))
        }
    }
}
