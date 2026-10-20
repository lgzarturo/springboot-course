package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.features.reservations.dto.CreateReservationRequest
import com.lgzarturo.springbootcourse.features.reservations.dto.UpdateReservationStatusRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate

@TestConfiguration
class ReservationMockkTestConfig {
    @Bean
    fun reservationService(): ReservationService = mockk()
}

/**
 * Tests del controlador REST de reservas.
 * Verifica creación, detalle, listado con filtros, cambios de estado y errores.
 */
@WebMvcTest(ReservationController::class)
@Import(ReservationMockkTestConfig::class, SecurityTestConfig::class)
@DisplayName("ReservationController Tests")
class ReservationControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private val baseUrl = "/api/v1/reservations"
    private val checkIn = LocalDate.of(2026, 11, 1)
    private val checkOut = LocalDate.of(2026, 11, 4)

    private fun createRequest() =
        CreateReservationRequest(
            roomId = "room-1",
            userId = 1L,
            guestName = "Ash Ketchum",
            checkIn = checkIn,
            checkOut = checkOut,
        )

    private fun sampleReservation(status: ReservationStatus = ReservationStatus.PENDING) =
        Reservation(
            id = "res-1",
            roomId = "room-1",
            hotelId = "hotel-1",
            userId = 1L,
            guestName = "Ash Ketchum",
            checkIn = checkIn,
            checkOut = checkOut,
            totalPrice = 450.0,
            status = status,
        )

    @BeforeEach
    fun setUp() {
        clearMocks(reservationService)
    }

    @Test
    @DisplayName("POST debe crear una reserva y devolver 201 con el total calculado")
    fun `should create reservation and return 201`() {
        // Given
        every { reservationService.createReservation(any()) } returns sampleReservation()

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id", `is`("res-1")))
            .andExpect(jsonPath("$.totalPrice", `is`(450.0)))
            .andExpect(jsonPath("$.status", `is`("PENDING")))
    }

    @Test
    @DisplayName("POST debe devolver 400 con datos inválidos")
    fun `should return 400 with invalid reservation data`() {
        // Given: nombre en blanco
        val invalid = createRequest().copy(guestName = " ")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)),
            ).andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("POST debe devolver 400 con rango de fechas inválido y 409 con solape")
    fun `should return 400 on invalid range and 409 on overlap`() {
        // Given: rango inválido
        every { reservationService.createReservation(any()) } throws
            IllegalArgumentException("check-out debe ser posterior a check-in")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isBadRequest)

        // Given: solape con otra reserva
        every { reservationService.createReservation(any()) } throws
            IllegalStateException("La habitación ya está reservada en esas fechas")

        // When / Then
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest())),
            ).andExpect(status().isConflict)
    }

    @Test
    @DisplayName("GET debe devolver el detalle o 404 si no existe")
    fun `should return detail or 404 when missing`() {
        // Given
        every { reservationService.getReservationById("res-1") } returns sampleReservation()
        every { reservationService.getReservationById("missing") } returns null

        // When / Then
        mockMvc
            .perform(get("$baseUrl/res-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id", `is`("res-1")))
            .andExpect(jsonPath("$.guestName", `is`("Ash Ketchum")))

        mockMvc
            .perform(get("$baseUrl/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("GET listado debe paginar y filtrar por usuario, estado y fechas")
    fun `should list reservations paginated and filtered`() {
        // Given
        every { reservationService.getAllReservations(any(), any(), any()) } returns
            (listOf(sampleReservation()) to 1L)

        // When / Then
        mockMvc
            .perform(get("$baseUrl?userId=1&status=PENDING&from=2026-11-01&to=2026-11-30&page=0&size=10"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content", hasSize<Any>(1)))
            .andExpect(jsonPath("$.totalElements", `is`(1)))
    }

    @Test
    @DisplayName("PATCH debe cambiar el estado o devolver 404 y 409 según el caso")
    fun `should update status or return 404 and 409`() {
        // Given: confirmación válida
        every { reservationService.updateStatus("res-1", ReservationStatus.CONFIRMED) } returns
            sampleReservation(status = ReservationStatus.CONFIRMED)
        every { reservationService.updateStatus("missing", ReservationStatus.CONFIRMED) } returns null
        every { reservationService.updateStatus("res-1", ReservationStatus.COMPLETED) } throws
            IllegalStateException("Transición inválida")

        val confirm = UpdateReservationStatusRequest(status = ReservationStatus.CONFIRMED)
        val invalid = UpdateReservationStatusRequest(status = ReservationStatus.COMPLETED)

        // When / Then
        mockMvc
            .perform(
                patch("$baseUrl/res-1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(confirm)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status", `is`("CONFIRMED")))

        mockMvc
            .perform(
                patch("$baseUrl/missing/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(confirm)),
            ).andExpect(status().isNotFound)

        mockMvc
            .perform(
                patch("$baseUrl/res-1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalid)),
            ).andExpect(status().isConflict)
    }
}
