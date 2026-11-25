package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.auth.AuthService
import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.rooms.dto.CreateRoomRequest
import com.lgzarturo.springbootcourse.features.rooms.dto.RoomResponse
import com.lgzarturo.springbootcourse.features.rooms.dto.UpdateRoomRequest
import com.lgzarturo.springbootcourse.features.users.CreateUserCommand
import com.lgzarturo.springbootcourse.features.users.CreateUserService
import com.lgzarturo.springbootcourse.features.users.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.DefaultUriBuilderFactory

/**
 * Pruebas de integración del flujo completo de habitaciones.
 * Cubre CRUD anidado, validación, paginación, filtros y OpenAPI.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        // El application.yaml de tests oculta al principal: replicar la ruta de producción aquí.
        "springdoc.api-docs.path=/api-docs",
    ],
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@DisplayName("Room Integration Tests")
class RoomIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var createUserService: CreateUserService

    @Autowired
    private lateinit var authService: AuthService

    private val testRestTemplate: TestRestTemplate by lazy {
        val template = TestRestTemplate()
        val factory = DefaultUriBuilderFactory("http://localhost:$port")
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        template.restTemplate.uriTemplateHandler = factory
        template.restTemplate.interceptors.add(
            ClientHttpRequestInterceptor { request, body, execution ->
                request.headers.setBearerAuth(staffToken())
                execution.execute(request, body)
            },
        )
        template
    }

    private val staffTokenValue: String by lazy {
        val email = "rooms.it.${System.nanoTime()}@pokemon.com"
        createUserService.execute(
            CreateUserCommand(
                email = email,
                password = "Password123!",
                firstName = "Staff",
                lastName = "Member",
                phoneNumber = null,
                role = UserRole.STAFF,
            ),
        )
        authService.authenticate(email, "Password123!")
    }

    private fun staffToken(): String = staffTokenValue

    private fun createHotel(): String {
        val response =
            testRestTemplate.postForEntity(
                "/api/v1/hotels",
                CreateHotelRequest("Hotel Rooms E2E", "Calle E2E 1"),
                HotelResponse::class.java,
            )
        assertEquals(HttpStatus.CREATED, response.statusCode)
        return response.body?.id ?: error("Hotel sin id")
    }

    private fun createRequest() =
        CreateRoomRequest(
            number = "101",
            type = "SUITE",
            status = RoomStatus.AVAILABLE,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            price = 150.0,
            description = "Suite con vista al lago",
        )

    private fun jsonEntity(body: Any): HttpEntity<Any> =
        HttpEntity(body, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON })

    @Test
    @DisplayName("POST debe crear una habitación bajo el hotel y devolver 201")
    fun `should create room under hotel and return 201`() {
        // Given
        val hotelId = createHotel()

        // When
        val response =
            testRestTemplate.postForEntity("/api/v1/hotels/$hotelId/rooms", createRequest(), RoomResponse::class.java)

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body?.id)
        assertEquals("101", response.body?.number)
        assertEquals(RoomStatus.AVAILABLE, response.body?.status)
        assertEquals(PokemonTheme.PIKACHU, response.body?.theme)
        assertEquals(hotelId, response.body?.hotelId)
    }

    @Test
    @DisplayName("POST debe devolver 400 cuando el hotel no existe")
    fun `should return 400 when hotel does not exist`() {
        // When
        val response =
            testRestTemplate.postForEntity(
                "/api/v1/hotels/hotel-inexistente/rooms",
                createRequest(),
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    @DisplayName("POST debe devolver 400 con datos inválidos")
    fun `should return 400 with invalid room data`() {
        // Given
        val hotelId = createHotel()
        val invalid = createRequest().copy(number = " ", capacity = 0)

        // When
        val response =
            testRestTemplate.postForEntity("/api/v1/hotels/$hotelId/rooms", invalid, String::class.java)

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    @DisplayName("GET debe devolver detalle, actualizar y eliminar la habitación")
    fun `should get update and delete room detail`() {
        // Given
        val hotelId = createHotel()
        val created =
            testRestTemplate
                .postForEntity("/api/v1/hotels/$hotelId/rooms", createRequest(), RoomResponse::class.java)
                .body ?: error("Sin cuerpo")
        val roomId = created.id ?: error("Sin id")

        // When: detalle
        val detail =
            testRestTemplate.getForEntity("/api/v1/hotels/$hotelId/rooms/$roomId", RoomResponse::class.java)

        // Then
        assertEquals(HttpStatus.OK, detail.statusCode)
        assertEquals("101", detail.body?.number)

        // When: actualizar
        val update =
            UpdateRoomRequest(
                number = "102",
                type = "DOUBLE",
                status = RoomStatus.OCCUPIED,
                theme = PokemonTheme.CHARIZARD,
                capacity = 4,
                price = 200.0,
                description = "Vista al volcán",
            )
        val updated =
            testRestTemplate.exchange(
                "/api/v1/hotels/$hotelId/rooms/$roomId",
                HttpMethod.PUT,
                jsonEntity(update),
                RoomResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, updated.statusCode)
        assertEquals("102", updated.body?.number)
        assertEquals(4, updated.body?.capacity)

        // When: eliminar
        val deleted =
            testRestTemplate.exchange(
                "/api/v1/hotels/$hotelId/rooms/$roomId",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, deleted.statusCode)
        val afterDelete =
            testRestTemplate.getForEntity("/api/v1/hotels/$hotelId/rooms/$roomId", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, afterDelete.statusCode)
    }

    @Test
    @DisplayName("GET listado debe paginar y filtrar por estado y tema")
    fun `should list rooms paginated and filtered`() {
        // Given
        val hotelId = createHotel()
        testRestTemplate.postForEntity("/api/v1/hotels/$hotelId/rooms", createRequest(), RoomResponse::class.java)
        testRestTemplate.postForEntity(
            "/api/v1/hotels/$hotelId/rooms",
            createRequest().copy(number = "102", status = RoomStatus.OCCUPIED, theme = PokemonTheme.CHARIZARD),
            RoomResponse::class.java,
        )

        // When
        val response =
            testRestTemplate.getForEntity(
                "/api/v1/hotels/$hotelId/rooms?page=0&size=10&status=OCCUPIED&theme=CHARIZARD",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.contains("\"number\":\"102\"") == true)
        assertTrue(response.body?.contains("\"totalElements\":1") == true)
    }

    @Test
    @DisplayName("OpenAPI debe documentar los endpoints de habitaciones")
    fun `should expose room endpoints in OpenAPI document`() {
        // When
        val response = testRestTemplate.getForEntity("/api-docs", String::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertTrue(response.body?.contains("/api/v1/hotels/{hotelId}/rooms") == true)
        assertTrue(response.body?.contains("/api/v1/hotels/{hotelId}/rooms/{roomId}") == true)
    }
}
