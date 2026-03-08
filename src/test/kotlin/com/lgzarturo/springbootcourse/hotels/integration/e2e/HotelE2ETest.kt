@file:Suppress("LongMethod")

package com.lgzarturo.springbootcourse.hotels.integration.e2e

import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.CreateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.UpdateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.HotelResponse
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.PageResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

/**
 * Pruebas E2E para la gestión de hoteles.
 * Valora el comportamiento completo del sistema desde la perspectiva del usuario.
 * Utiliza TestRestTemplate para realizar las peticiones HTTP.
 * Usa el perfil de tests para que no se ejecuten las transacciones de base de datos.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // Usa H2
@ActiveProfiles("tests")
class HotelE2ETest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    private val baseUrl = "/api/v1/hotels"

    @Nested
    @DisplayName("E2E Escenarios de Gestión de Hoteles")
    inner class HotelManagementScenarios {
        @Test
        @DisplayName("Escenario: Crear, Leer, Actualizar, Eliminar un Hotel")
        fun `e2e create read update delete hotel`() {
            // --- Escenario: Un administrador crea un nuevo hotel ---
            println("--- Escenario: Crear Hotel ---")
            val createRequest = CreateHotelRequest("Hotel Plaza Pokémon", "123 Main ZH, Downtown")
            val createResponse = testRestTemplate.postForEntity(baseUrl, createRequest, HotelResponse::class.java)

            Assertions.assertEquals(HttpStatus.OK, createResponse.statusCode, "El hotel debería crearse exitosamente")
            val createdHotelId = createResponse.body?.id
            Assertions.assertNotNull(createdHotelId, "El ID del hotel creado no debería ser nulo")
            Assertions.assertEquals("Hotel Plaza Pokémon", createResponse.body?.name)
            Assertions.assertEquals("123 Main ZH, Downtown", createResponse.body?.address)
            println("Hotel creado con ID: $createdHotelId")

            // --- Escenario: El administrador consulta la lista de hoteles y ve el nuevo hotel ---
            println("--- Escenario: Consultar Lista de Hoteles ---")
            val listResponse = testRestTemplate.getForEntity(baseUrl, PageResponse::class.java)

            Assertions.assertEquals(HttpStatus.OK, listResponse.statusCode)
            Assertions.assertTrue(
                listResponse.body?.content?.any {
                    it.id == createdHotelId
                } == true,
                "El hotel recién creado debería estar en la lista",
            )
            println("Lista de hoteles verificada.")

            // --- Escenario: El administrador actualiza la información del hotel ---
            println("--- Escenario: Actualizar Hotel ---")
            val updateRequest =
                UpdateHotelRequest("Hotel Plaza Pokémon Renovado", "123 Main ZH, Downtown, Renovated Wing")
            val updateResponse =
                testRestTemplate.exchange(
                    "$baseUrl/$createdHotelId",
                    HttpMethod.PUT,
                    HttpEntity(updateRequest, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
                    HotelResponse::class.java,
                )

            Assertions.assertEquals(HttpStatus.OK, updateResponse.statusCode, "La actualización debería ser exitosa")
            Assertions.assertEquals(
                createdHotelId,
                updateResponse.body?.id,
                "El ID no debería cambiar tras la actualización",
            )
            Assertions.assertEquals(
                "Hotel Plaza Pokémon Renovado",
                updateResponse.body?.name,
                "El nombre debería actualizarse",
            )
            Assertions.assertEquals(
                "123 Main ZH, Downtown, Renovated Wing",
                updateResponse.body?.address,
                "La dirección debería actualizarse",
            )
            println("Hotel actualizado.")

            // --- Escenario: El administrador consulta el hotel específico y ve la información actualizada ---
            println("--- Escenario: Consultar Hotel Específico ---")
            val getResponse = testRestTemplate.getForEntity("$baseUrl/$createdHotelId", HotelResponse::class.java)

            Assertions.assertEquals(HttpStatus.OK, getResponse.statusCode)
            Assertions.assertEquals(createdHotelId, getResponse.body?.id)
            Assertions.assertEquals("Hotel Plaza Pokémon Renovado", getResponse.body?.name)
            Assertions.assertEquals("123 Main ZH, Downtown, Renovated Wing", getResponse.body?.address)
            println("Información del hotel verificada.")

            // --- Escenario: El administrador decide eliminar el hotel ---
            println("--- Escenario: Eliminar Hotel ---")
            val deleteResponse =
                testRestTemplate.exchange(
                    "$baseUrl/$createdHotelId",
                    HttpMethod.DELETE,
                    HttpEntity.EMPTY,
                    String::class.java,
                )

            Assertions.assertEquals(
                HttpStatus.NO_CONTENT,
                deleteResponse.statusCode,
                "La eliminación debería ser exitosa y devolver 204 No Content",
            )
            println("Hotel eliminado.")

            // --- Escenario: El administrador intenta consultar el hotel eliminado y recibe 404 ---
            println("--- Escenario: Verificar Eliminación ---")
            val getAfterDeleteResponse = testRestTemplate.getForEntity("$baseUrl/$createdHotelId", String::class.java)

            Assertions.assertEquals(
                HttpStatus.NOT_FOUND,
                getAfterDeleteResponse.statusCode,
                "El hotel eliminado no debería encontrarse",
            )
            println("Eliminación confirmada: el hotel ya no existe.")
        }

        @Test
        @DisplayName("Escenario: Buscar y Filtrar Hoteles")
        fun `e2e search and filter hotels`() {
            // Dado que existen hoteles en el sistema
            testRestTemplate.postForEntity(
                baseUrl,
                CreateHotelRequest("Plaza Hotel", "Downtown"),
                HotelResponse::class.java,
            )
            testRestTemplate.postForEntity(
                baseUrl,
                CreateHotelRequest("Beach Resort", "Seaside"),
                HotelResponse::class.java,
            )
            testRestTemplate.postForEntity(
                baseUrl,
                CreateHotelRequest("Mountain Lodge", "Uptown"),
                HotelResponse::class.java,
            )

            // --- Escenario: Un usuario busca hoteles por nombre ---
            println("--- Escenario: Buscar Hoteles por Nombre ---")
            val searchResponse = testRestTemplate.getForEntity("$baseUrl?name=Plaza", PageResponse::class.java)

            Assertions.assertEquals(HttpStatus.OK, searchResponse.statusCode)
            val searchResults = searchResponse.body?.content
            Assertions.assertNotNull(searchResults)
            Assertions.assertTrue(
                searchResults!!.any { it.name.contains("Plaza", ignoreCase = true) },
                "Debería haber al menos un hotel con 'Plaza' en el nombre",
            )
            Assertions.assertTrue(searchResults.size == 1, "Debería haber solo un hotel coincidente con 'Plaza'")
            println("Búsqueda por nombre verificada.")

            // --- Escenario: Un usuario página la lista de hoteles ---
            println("--- Escenario: Paginar Lista de Hoteles ---")
            val pageResponse = testRestTemplate.getForEntity("$baseUrl?page=0&size=2", PageResponse::class.java)

            Assertions.assertEquals(HttpStatus.OK, pageResponse.statusCode)
            Assertions.assertEquals(2, pageResponse.body?.content?.size, "La página debería contener 2 elementos")
            Assertions.assertEquals(3L, pageResponse.body?.totalElements, "El total de elementos debería ser 3")
            println("Paginación verificada.")
        }
    }
}
