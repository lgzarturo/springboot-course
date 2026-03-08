package com.lgzarturo.springbootcourse.hotels.integration

import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.CreateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.UpdateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.HotelResponse
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.PageResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

/**
 * Pruebas de integración para el controlador de hoteles.
 * Valora el comportamiento de los endpoints REST.
 * Utiliza TestRestTemplate para realizar las peticiones HTTP.
 * Usan el perfil de tests para que no se ejecuten las transacciones de base de datos.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Usa H2
@ActiveProfiles("tests")
class HotelIntegrationTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    private val baseUrl = "/api/v1/hotels"

    @BeforeEach
    fun setUp() {
        // Opcional: Limpiar la base de datos antes de cada test
        // Se puede hacer con @Sql o un servicio de limpieza si lo tienes
    }

    @Test
    @DisplayName("POST /api/v1/hotels debe crear un hotel correctamente y devolver 200 OK")
    fun `should create a hotel and return 200 OK`() {
        // Given
        val request = CreateHotelRequest("Test Hotel", "Test Address")

        // When
        val response = testRestTemplate.postForEntity(baseUrl, request, HotelResponse::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Test Hotel", response.body?.name)
        assertEquals("Test Address", response.body?.address)
        assertNotNull(response.body?.id)
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{id} debe devolver el hotel si se encuentra y devolver 200 OK")
    fun `should return hotel if found and return 200 OK`() {
        // Given: Crea un hotel primero
        val createRequest = CreateHotelRequest("Test Hotel Get", "Address Get")
        val createResponse = testRestTemplate.postForEntity(baseUrl, createRequest, HotelResponse::class.java)
        val hotelId = createResponse.body?.id!!
        assertNotNull(hotelId)

        // When
        val response = testRestTemplate.getForEntity("$baseUrl/$hotelId", HotelResponse::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(hotelId, response.body?.id)
        assertEquals("Test Hotel Get", response.body?.name)
        assertEquals("Address Get", response.body?.address)
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{id} debe devolver 404 Not Found si el hotel no existe")
    fun `should return 404 Not Found if hotel does not exist`() {
        // Given
        val nonExistentId = "non-existent-id"

        // When
        val response = testRestTemplate.getForEntity("$baseUrl/$nonExistentId", String::class.java)

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    @DisplayName("GET /api/v1/hotels debe devolver lista paginada y devolver 200 OK")
    fun `should return paginated list and return 200 OK`() {
        // Given: Crea algunos hoteles
        testRestTemplate.postForEntity(baseUrl, CreateHotelRequest("Hotel 1", "Addr 1"), HotelResponse::class.java)
        testRestTemplate.postForEntity(baseUrl, CreateHotelRequest("Hotel 2", "Addr 2"), HotelResponse::class.java)

        // When
        val response = testRestTemplate.getForEntity("$baseUrl?page=0&size=10", PageResponse::class.java)

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        val pageResponse = response.body!!
        assertTrue(pageResponse.content.size >= 2) // Debe haber al menos los 2 creados
        assertEquals(2L, pageResponse.totalElements) // Asumiendo que solo se crearon 2
        assertEquals(1, pageResponse.totalPages)
        assertTrue(pageResponse.first)
        assertTrue(pageResponse.last)
    }

    @Test
    @DisplayName("PUT /api/v1/hotels/{id} debe actualizar un hotel y devolver 200 OK")
    fun `should update hotel and return 200 OK`() {
        // Given: Crea un hotel
        val createRequest = CreateHotelRequest("Original Hotel", "Original Address")
        val createResponse = testRestTemplate.postForEntity(baseUrl, createRequest, HotelResponse::class.java)
        val hotelId = createResponse.body?.id!!
        assertNotNull(hotelId)

        // When: Actualiza el hotel
        val updateRequest = UpdateHotelRequest("Updated Hotel", "Updated Address")
        val updateResponse =
            testRestTemplate.exchange(
                "$baseUrl/$hotelId",
                HttpMethod.PUT,
                HttpEntity(updateRequest, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
                HotelResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, updateResponse.statusCode)
        assertNotNull(updateResponse.body)
        assertEquals(hotelId, updateResponse.body?.id)
        assertEquals("Updated Hotel", updateResponse.body?.name)
        assertEquals("Updated Address", updateResponse.body?.address)
    }

    @Test
    @DisplayName("PUT /api/v1/hotels/{id} debe devolver 404 Not Found si el hotel no existe")
    fun `should return 404 Not Found if hotel does not exist on update`() {
        // Given
        val nonExistentId = "non-existent-id"
        val updateRequest = UpdateHotelRequest("Updated Hotel", "Updated Address")

        // When
        val response =
            testRestTemplate.exchange(
                "$baseUrl/$nonExistentId",
                HttpMethod.PUT,
                HttpEntity(updateRequest, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
                HotelResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    @DisplayName("DELETE /api/v1/hotels/{id} debe eliminar un hotel y devolver 204 No Content")
    fun `should delete hotel and return 204 No Content`() {
        // Given: Crea un hotel
        val createRequest = CreateHotelRequest("Hotel to Delete", "Address to Delete")
        val createResponse = testRestTemplate.postForEntity(baseUrl, createRequest, HotelResponse::class.java)
        val hotelId = createResponse.body?.id!!
        assertNotNull(hotelId)

        // When: Elimina el hotel
        val response =
            testRestTemplate.exchange(
                "$baseUrl/$hotelId",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // And Then: Verifica que ya no exista
        val getResponse = testRestTemplate.getForEntity("$baseUrl/$hotelId", String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @Test
    @DisplayName("DELETE /api/v1/hotels/{id} debe devolver 404 Not Found si el hotel no existe")
    fun `should return 404 Not Found if hotel does not exist on delete`() {
        // Given
        val nonExistentId = "non-existent-id"

        // When
        val response =
            testRestTemplate.exchange(
                "$baseUrl/$nonExistentId",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
