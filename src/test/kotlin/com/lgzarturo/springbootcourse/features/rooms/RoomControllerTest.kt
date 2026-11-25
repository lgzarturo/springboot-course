package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.rooms.dto.CreateRoomRequest
import com.lgzarturo.springbootcourse.features.rooms.dto.UpdateRoomRequest
import com.lgzarturo.springbootcourse.security.SecurityTestConfig
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@TestConfiguration
class RoomMockkTestConfig {
    @Bean
    fun roomService(): RoomService = mockk()
}

/**
 * Tests del controlador REST de habitaciones.
 * Verifica CRUD anidado, validación, paginación y filtros.
 */
@WebMvcTest(RoomController::class)
@Import(RoomMockkTestConfig::class, SecurityTestConfig::class)
@DisplayName("RoomController Tests")
class RoomControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val hotelId = "hotel-1"
    private val baseUrl = "/api/v1/hotels/$hotelId/rooms"

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

    private fun sampleRoom() =
        Room(
            id = "room-1",
            number = "101",
            type = "SUITE",
            price = 150.0,
            hotelId = hotelId,
            status = RoomStatus.AVAILABLE,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            description = "Suite con vista al lago",
        )

    @BeforeEach
    fun setUp() {
        clearMocks(roomService, answers = false)
    }

    @Test
    @DisplayName("POST debe crear una habitación y devolver 201 Created")
    fun `should create room and return 201`() {
        // Given
        val request = createRequest()

        // When
        every { roomService.createRoom(hotelId, any()) } returns sampleRoom()

        // Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`("room-1")))
            .andExpect(jsonPath("$.number", `is`("101")))
            .andExpect(jsonPath("$.status", `is`("AVAILABLE")))
            .andExpect(jsonPath("$.theme", `is`("PIKACHU")))
            .andExpect(jsonPath("$.capacity", `is`(2)))
            .andExpect(jsonPath("$.hotelId", `is`(hotelId)))
    }

    @Test
    @DisplayName("POST debe devolver 400 cuando el hotel no existe")
    fun `should return 400 when hotel does not exist`() {
        // Given
        val request = createRequest()

        // When
        every { roomService.createRoom(hotelId, any()) } throws IllegalArgumentException("Hotel no encontrado")

        // Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("POST debe devolver 400 con número en blanco")
    fun `should return 400 when number is blank`() {
        // Given
        val request = createRequest().copy(number = "  ")

        // Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("POST debe devolver 400 con capacidad no positiva")
    fun `should return 400 when capacity is not positive`() {
        // Given
        val request = createRequest().copy(capacity = 0)

        // Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("POST debe devolver 400 con un estado inválido")
    fun `should return 400 when status value is invalid`() {
        // Given: JSON crudo con un enum inexistente
        val rawJson =
            """
            {"number":"101","type":"SUITE","status":"DRAGON","theme":"PIKACHU",
             "capacity":2,"price":150.0,"description":"Suite"}
            """.trimIndent()

        // Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(rawJson),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("GET detalle debe devolver la habitación y 200")
    fun `should return room detail and 200`() {
        // Given
        every { roomService.getRoomById(hotelId, "room-1") } returns sampleRoom()

        // Then
        mockMvc
            .perform(get("$baseUrl/room-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", `is`("room-1")))
            .andExpect(jsonPath("$.number", `is`("101")))
    }

    @Test
    @DisplayName("GET detalle debe devolver 404 si no existe")
    fun `should return 404 when room not found`() {
        // Given
        every { roomService.getRoomById(hotelId, "missing") } returns null

        // Then
        mockMvc
            .perform(get("$baseUrl/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET listado debe paginar y filtrar por estado y tema")
    fun `should return paginated list filtered by status and theme`() {
        // Given
        val criteria = RoomSearchCriteria(status = RoomStatus.AVAILABLE, theme = PokemonTheme.PIKACHU)

        // When
        every { roomService.getAllRooms(hotelId, criteria, 0, 10) } returns Pair(listOf(sampleRoom()), 1)

        // Then
        mockMvc
            .perform(get("$baseUrl?page=0&size=10&status=AVAILABLE&theme=PIKACHU"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Int>(1)))
            .andExpect(jsonPath("$.totalElements", `is`(1)))
            .andExpect(jsonPath("$.content[0].id", `is`("room-1")))
    }

    @Test
    @DisplayName("PUT debe actualizar y devolver 200")
    fun `should update room and return 200`() {
        // Given
        val request =
            UpdateRoomRequest(
                number = "102",
                type = "DOUBLE",
                status = RoomStatus.OCCUPIED,
                theme = PokemonTheme.CHARIZARD,
                capacity = 4,
                price = 200.0,
                description = "Vista al volcán",
            )
        val updated = sampleRoom().copy(number = "102", capacity = 4)

        // When
        every { roomService.updateRoom(hotelId, "room-1", any()) } returns updated

        // Then
        mockMvc
            .perform(
                put("$baseUrl/room-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.number", `is`("102")))
            .andExpect(jsonPath("$.capacity", `is`(4)))
    }

    @Test
    @DisplayName("PUT debe devolver 404 si la habitación no existe")
    fun `should return 404 on update when room not found`() {
        // Given
        val request =
            UpdateRoomRequest(
                number = "102",
                type = "DOUBLE",
                status = RoomStatus.OCCUPIED,
                theme = PokemonTheme.CHARIZARD,
                capacity = 4,
                price = 200.0,
                description = null,
            )

        // When
        every { roomService.updateRoom(hotelId, "missing", any()) } returns null

        // Then
        mockMvc
            .perform(
                put("$baseUrl/missing")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("DELETE debe eliminar y devolver 204")
    fun `should delete room and return 204`() {
        // Given
        every { roomService.deleteRoom(hotelId, "room-1") } returns true

        // Then
        mockMvc
            .perform(delete("$baseUrl/room-1"))
            .andExpect(status().isNoContent)
    }

    @Test
    @DisplayName("DELETE debe devolver 404 si la habitación no existe")
    fun `should return 404 on delete when room not found`() {
        // Given
        every { roomService.deleteRoom(hotelId, "missing") } returns false

        // Then
        mockMvc
            .perform(delete("$baseUrl/missing"))
            .andExpect(status().isNotFound)
    }
}
