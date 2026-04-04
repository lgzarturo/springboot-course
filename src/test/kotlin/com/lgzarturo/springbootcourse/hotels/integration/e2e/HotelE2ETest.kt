@file:Suppress("LongMethod")

package com.lgzarturo.springbootcourse.hotels.integration.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.CreateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.UpdateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.HotelResponse
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response.PageResponse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
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
import org.springframework.test.context.ActiveProfiles

/**
 * Pruebas E2E para la gestión de hoteles.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class HotelE2ETest {
    @LocalServerPort
    private var port: Int = 0

    private val testRestTemplate: TestRestTemplate by lazy {
        val template = TestRestTemplate()
        val factory =
            org.springframework.web.util
                .DefaultUriBuilderFactory("http://localhost:$port")
        factory.encodingMode = org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode.NONE
        template.restTemplate.uriTemplateHandler = factory
        template
    }

    private val baseUrl = "/api/v1/hotels"
    private val mapper = ObjectMapper()

    private fun getHotels(): PageResponse<HotelResponse> {
        val response = testRestTemplate.getForEntity(baseUrl, String::class.java)
        val type =
            TypeFactory.defaultInstance().constructParametricType(
                PageResponse::class.java,
                HotelResponse::class.java,
            )
        return mapper.readValue(response.body!!, type)
    }

    private fun createHotel(request: CreateHotelRequest): HotelResponse {
        val response = testRestTemplate.postForEntity(baseUrl, request, String::class.java)
        return mapper.readValue(response.body!!, HotelResponse::class.java)
    }

    @Nested
    @DisplayName("E2E Escenarios de Gestión de Hoteles")
    inner class HotelManagementScenarios {
        @Test
        @DisplayName("Escenario: Crear, Leer, Actualizar, Eliminar un Hotel")
        fun `e2e create read update delete hotel`() {
            println("--- Escenario: Crear Hotel ---")
            val createRequest = CreateHotelRequest("Hotel Plaza Pokémon", "123 Main ZH, Downtown")
            val createdHotel = createHotel(createRequest)

            Assertions.assertEquals(
                HttpStatus.OK,
                testRestTemplate.postForEntity(baseUrl, createRequest, String::class.java).statusCode,
            )
            Assertions.assertNotNull(createdHotel.id, "El ID del hotel creado no debería ser nulo")
            Assertions.assertEquals("Hotel Plaza Pokémon", createdHotel.name)
            Assertions.assertEquals("123 Main ZH, Downtown", createdHotel.address)
            println("Hotel creado con ID: ${createdHotel.id}")

            println("--- Escenario: Consultar Lista de Hoteles ---")
            val listResponse = getHotels()

            Assertions.assertTrue(
                listResponse.content.any { it.id == createdHotel.id },
                "El hotel recién creado debería estar en la lista",
            )
            println("Lista de hoteles verificada.")

            println("--- Escenario: Actualizar Hotel ---")
            val updateRequest =
                UpdateHotelRequest("Hotel Plaza Pokémon Renovado", "123 Main ZH, Downtown, Renovated Wing")
            val updateResponse =
                testRestTemplate.exchange(
                    "$baseUrl/${createdHotel.id}",
                    HttpMethod.PUT,
                    HttpEntity(updateRequest, HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }),
                    String::class.java,
                )

            Assertions.assertEquals(HttpStatus.OK, updateResponse.statusCode, "La actualización debería ser exitosa")
            val updatedHotel = mapper.readValue(updateResponse.body!!, HotelResponse::class.java)
            Assertions.assertEquals(createdHotel.id, updatedHotel.id, "El ID no debería cambiar tras la actualización")
            Assertions.assertEquals("Hotel Plaza Pokémon Renovado", updatedHotel.name, "El nombre debería actualizarse")
            Assertions.assertEquals(
                "123 Main ZH, Downtown, Renovated Wing",
                updatedHotel.address,
                "La dirección debería actualizarse",
            )
            println("Hotel actualizado.")

            println("--- Escenario: Consultar Hotel Específico ---")
            val getResponse = testRestTemplate.getForEntity("$baseUrl/${createdHotel.id}", String::class.java)

            Assertions.assertEquals(HttpStatus.OK, getResponse.statusCode)
            val fetchedHotel = mapper.readValue(getResponse.body!!, HotelResponse::class.java)
            Assertions.assertEquals(createdHotel.id, fetchedHotel.id)
            Assertions.assertEquals("Hotel Plaza Pokémon Renovado", fetchedHotel.name)
            Assertions.assertEquals("123 Main ZH, Downtown, Renovated Wing", fetchedHotel.address)
            println("Información del hotel verificada.")

            println("--- Escenario: Eliminar Hotel ---")
            val deleteResponse =
                testRestTemplate.exchange(
                    "$baseUrl/${createdHotel.id}",
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

            println("--- Escenario: Verificar Eliminación ---")
            val getAfterDeleteResponse =
                testRestTemplate.getForEntity(
                    "$baseUrl/${createdHotel.id}",
                    String::class.java,
                )

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
            createHotel(CreateHotelRequest("Plaza Hotel", "Downtown"))
            createHotel(CreateHotelRequest("Beach Resort", "Seaside"))
            createHotel(CreateHotelRequest("Mountain Lodge", "Uptown"))

            println("--- Escenario: Buscar Hoteles por Nombre ---")
            val searchResponse = testRestTemplate.getForEntity("$baseUrl?name=Plaza", String::class.java)

            Assertions.assertEquals(HttpStatus.OK, searchResponse.statusCode)
            val typeFactory = TypeFactory.defaultInstance()
            val pageType = typeFactory.constructParametricType(PageResponse::class.java, HotelResponse::class.java)
            val searchResults =
                (
                    mapper.readValue(
                        searchResponse.body!!,
                        pageType,
                    ) as PageResponse<HotelResponse>
                ).content
            Assertions.assertNotNull(searchResults)
            Assertions.assertTrue(
                searchResults.any { it.name.contains("Plaza", ignoreCase = true) },
                "Debería haber al menos un hotel con 'Plaza' en el nombre",
            )
            Assertions.assertTrue(searchResults.size >= 1, "Debería haber al menos un hotel coincidente con 'Plaza'")
            println("Búsqueda por nombre verificada.")

            println("--- Escenario: Paginar Lista de Hoteles ---")
            val pageResponse = testRestTemplate.getForEntity("$baseUrl?page=0&size=2", String::class.java)

            Assertions.assertEquals(HttpStatus.OK, pageResponse.statusCode)
            val pageData = mapper.readValue(pageResponse.body!!, pageType) as PageResponse<HotelResponse>
            Assertions.assertEquals(2, pageData.content.size, "La página debería contener 2 elementos")
            Assertions.assertTrue(pageData.totalElements >= 3, "El total de elementos debería ser al menos 3")
            println("Paginación verificada.")
        }
    }
}
