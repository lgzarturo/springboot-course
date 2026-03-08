package com.lgzarturo.springbootcourse.hotels.adapters.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.CreateHotelRequest
import com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.request.UpdateHotelRequest
import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.hotels.domain.HotelSearchCriteria
import com.lgzarturo.springbootcourse.hotels.service.HotelService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests de integración para HotelController
 * Verifica el comportamiento de los endpoints REST
 */
@WebMvcTest(HotelController::class)
@DisplayName("HotelController Integration Tests")
class HotelControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var hotelService: HotelService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("POST /api/v1/hotels debe crear un nuevo hotel y devolver 200")
    fun `should create hotel and return 200`() {
        // Given
        val request = CreateHotelRequest("New Hotel", "New Address")
        val savedHotel = Hotel("1", "New Hotel", "New Address", emptyList())
        val expectedJson = """{"id":"1","name":"New Hotel","address":"New Address","_links":{}}"""

        // When
        every { hotelService.createHotel(any()) } returns savedHotel

        // Then
        mockMvc
            .perform(
                post("/api/v1/hotels")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").exists())
            .andExpect(jsonPath("$.id", `is`("1")))
            .andExpect(jsonPath("$.name", `is`("New Hotel")))
            .andExpect(jsonPath("$.address", `is`("New Address")))
            .andExpect { result ->
                val responseJson = result.response.contentAsString
                assert(responseJson == expectedJson)
            }
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{id} debe devolver el hotel si existe y devolver 200")
    fun `should return hotel if found and return 200`() {
        // Given
        val hotelId = "1"
        val hotel = Hotel("1", "Test Hotel", "Test Address", emptyList())
        val expectedJson = """{"id":"1","name":"Test Hotel","address":"Test Address","_links":{}}"""

        // When
        every { hotelService.getHotelById(hotelId) } returns hotel

        // Then
        mockMvc
            .perform(get("/api/v1/hotels/$hotelId"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`("1")))
            .andExpect(jsonPath("$.name", `is`("Test Hotel")))
            .andExpect { result ->
                val responseJson = result.response.contentAsString
                assert(responseJson == expectedJson)
            }
    }

    @Test
    @DisplayName("GET /api/v1/hotels/{id} debe devolver 404 si no existe")
    fun `should return 404 if hotel not found`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelService.getHotelById(hotelId) } returns null

        // Then
        mockMvc
            .perform(get("/api/v1/hotels/$hotelId"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET /api/v1/hotels debe devolver una lista paginada de hoteles y devolver 200")
    fun `should return paginated list and return 200`() {
        // Given
        val criteria = HotelSearchCriteria(name = "Test")
        val page = 0
        val size = 10
        val hotels =
            listOf(
                Hotel("1", "Test Hotel 1", "Address 1", emptyList()),
                Hotel("2", "Test Hotel 2", "Address 2", emptyList()),
            )
        val totalElements = 2L

        // When
        every { hotelService.getAllHotels(eq(criteria), any(), any()) } returns Pair(hotels, totalElements)

        // Then
        mockMvc
            .perform(get("/api/v1/hotels?page=$page&size=$size&name=Test"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content", hasSize<Int>(2)))
            .andExpect(jsonPath("$.totalElements", `is`(2)))
            .andExpect(jsonPath("$.totalPages", `is`(1)))
            .andExpect(jsonPath("$.first", `is`(true)))
            .andExpect(jsonPath("$.last", `is`(true)))
    }

    @Test
    @DisplayName("PUT /api/v1/hotels/{id} debe actualizar el hotel y devolver 200")
    fun `should update hotel and return 200`() {
        // Given
        val hotelId = "1"
        val request = UpdateHotelRequest("Updated Name", "Updated Address")
        val updatedHotel = Hotel("1", "Updated Name", "Updated Address", emptyList())
        val expectedJson = """{"id":"1","name":"Updated Name","address":"Updated Address","_links":{}}"""

        // When
        every { hotelService.updateHotel(eq(hotelId), any()) } returns updatedHotel

        // Then
        mockMvc
            .perform(
                put("/api/v1/hotels/$hotelId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`("1")))
            .andExpect(jsonPath("$.name", `is`("Updated Name")))
            .andExpect { result ->
                val responseJson = result.response.contentAsString
                assert(responseJson == expectedJson)
            }
    }

    @Test
    @DisplayName("PUT /api/v1/hotels/{id} debe devolver 404 si no se encuentra el hotel")
    fun `should return 404 if hotel not found on update`() {
        // Given
        val hotelId = "non-existent-id"
        val request = UpdateHotelRequest("Updated Name", "Updated Address")

        // When
        every { hotelService.updateHotel(eq(hotelId), any()) } returns null

        // Then
        mockMvc
            .perform(
                put("/api/v1/hotels/$hotelId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("DELETE /api/v1/hotels/{id} debe eliminar el hotel y devolver 204")
    fun `should delete hotel and return 204`() {
        // Given
        val hotelId = "1"

        // When
        every { hotelService.deleteHotel(hotelId) } returns true

        // Then
        mockMvc
            .perform(delete("/api/v1/hotels/$hotelId"))
            .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("DELETE /api/v1/hotels/{id} debe devolver 404 si no se encuentra el hotel")
    fun `should return 404 if hotel not found on delete`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelService.deleteHotel(hotelId) } returns false

        // Then
        mockMvc
            .perform(delete("/api/v1/hotels/$hotelId"))
            .andExpect(status().isNotFound)
    }
}
