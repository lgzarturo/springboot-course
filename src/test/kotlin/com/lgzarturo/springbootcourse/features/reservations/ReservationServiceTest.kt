package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.rooms.PokemonTheme
import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.util.Optional

/**
 * Tests unitarios para ReservationService.
 * Verifica creación, solape, precio, transiciones y sincronización de habitación sin Spring.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("ReservationService Tests")
class ReservationServiceTest {
    @MockK
    private lateinit var reservationJpaRepository: ReservationJpaRepository

    @MockK
    private lateinit var roomJpaRepository: RoomJpaRepository

    private lateinit var reservationService: ReservationService

    private val hotelEntity = HotelEntity(id = "hotel-1", name = "Gran Hotel Pikachu", address = "Calle Trueno 25")
    private val checkIn = LocalDate.of(2026, 11, 1)
    private val checkOut = LocalDate.of(2026, 11, 4)

    private fun sampleRoomEntity(status: RoomStatus = RoomStatus.AVAILABLE) =
        RoomEntity(
            id = "room-1",
            number = "101",
            type = "SUITE",
            price = 150.0,
            hotel = hotelEntity,
            status = status,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            description = "Suite con vista al lago",
        )

    private fun sampleReservation(status: ReservationStatus = ReservationStatus.PENDING) =
        Reservation(
            id = "",
            roomId = "room-1",
            hotelId = "hotel-1",
            userId = 1L,
            guestName = "Ash Ketchum",
            checkIn = checkIn,
            checkOut = checkOut,
            totalPrice = 0.0,
            status = status,
        )

    private fun sampleEntity(
        id: String? = "res-1",
        status: ReservationStatus = ReservationStatus.PENDING,
    ) = ReservationEntity(
        id = id,
        room = sampleRoomEntity(),
        userId = 1L,
        guestName = "Ash Ketchum",
        checkIn = checkIn,
        checkOut = checkOut,
        totalPrice = 450.0,
        status = status,
    )

    @BeforeEach
    fun setUp() {
        reservationService = ReservationService(reservationJpaRepository, roomJpaRepository)
    }

    @Test
    @DisplayName("Debería crear la reserva con el total calculado y estado PENDING")
    fun `createReservation should save with computed total and pending status`() {
        // Given
        every { roomJpaRepository.findById("room-1") } returns Optional.of(sampleRoomEntity())
        every { reservationJpaRepository.existsOverlapping("room-1", checkIn, checkOut) } returns false
        every { reservationJpaRepository.save(any()) } returns sampleEntity()

        // When
        val result = reservationService.createReservation(sampleReservation())

        // Then: 3 noches x 150.0
        assertEquals(450.0, result.totalPrice)
        assertEquals(ReservationStatus.PENDING, result.status)
        assertEquals("res-1", result.id)
        verify { reservationJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar la creación cuando hay solape de fechas")
    fun `createReservation should throw when dates overlap`() {
        // Given
        every { roomJpaRepository.findById("room-1") } returns Optional.of(sampleRoomEntity())
        every { reservationJpaRepository.existsOverlapping("room-1", checkIn, checkOut) } returns true

        // When / Then
        assertThrows<IllegalStateException> { reservationService.createReservation(sampleReservation()) }
        verify(exactly = 0) { reservationJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar rangos inválidos y habitaciones inexistentes")
    fun `createReservation should reject invalid ranges and missing rooms`() {
        // Given: rango invertido
        val invalid = sampleReservation().copy(checkIn = checkOut, checkOut = checkIn)

        // When / Then: sin tocar repositorios
        assertThrows<IllegalArgumentException> { reservationService.createReservation(invalid) }
        verify(exactly = 0) { reservationJpaRepository.save(any()) }

        // Given: habitación inexistente
        every { roomJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertThrows<IllegalArgumentException> {
            reservationService.createReservation(sampleReservation().copy(roomId = "missing"))
        }
        verify(exactly = 0) { reservationJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería confirmar la reserva y marcar la habitación RESERVED")
    fun `updateStatus should confirm reservation and mark room reserved`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleEntity())
        every { roomJpaRepository.findById("room-1") } returns Optional.of(sampleRoomEntity())
        every { roomJpaRepository.save(any()) } returns sampleRoomEntity(status = RoomStatus.RESERVED)
        every { reservationJpaRepository.save(any()) } returns sampleEntity(status = ReservationStatus.CONFIRMED)

        // When
        val result = reservationService.updateStatus("res-1", ReservationStatus.CONFIRMED)

        // Then
        assertEquals(ReservationStatus.CONFIRMED, result?.status)
        verify { roomJpaRepository.save(match<RoomEntity> { it.status == RoomStatus.RESERVED }) }
        verify { reservationJpaRepository.save(match<ReservationEntity> { it.status == ReservationStatus.CONFIRMED }) }
    }

    @Test
    @DisplayName("Debería cancelar y restaurar la habitación a AVAILABLE")
    fun `updateStatus should cancel and restore room to available`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleEntity(status = ReservationStatus.CONFIRMED))
        every { roomJpaRepository.findById("room-1") } returns
            Optional.of(sampleRoomEntity(status = RoomStatus.RESERVED))
        every { roomJpaRepository.save(any()) } returns sampleRoomEntity(status = RoomStatus.AVAILABLE)
        every { reservationJpaRepository.save(any()) } returns sampleEntity(status = ReservationStatus.CANCELLED)

        // When
        val result = reservationService.updateStatus("res-1", ReservationStatus.CANCELLED)

        // Then
        assertEquals(ReservationStatus.CANCELLED, result?.status)
        verify { roomJpaRepository.save(match<RoomEntity> { it.status == RoomStatus.AVAILABLE }) }
    }

    @Test
    @DisplayName("Debería rechazar transiciones inválidas sin guardar nada")
    fun `updateStatus should reject invalid transitions without saving`() {
        // Given: salto PENDING -> COMPLETED
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleEntity())

        // When / Then
        assertThrows<IllegalStateException> { reservationService.updateStatus("res-1", ReservationStatus.COMPLETED) }
        verify(exactly = 0) { reservationJpaRepository.save(any()) }
        verify(exactly = 0) { roomJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería retornar null para reservas inexistentes")
    fun `should return null for missing reservations`() {
        // Given
        every { reservationJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertNull(reservationService.getReservationById("missing"))
        assertNull(reservationService.updateStatus("missing", ReservationStatus.CONFIRMED))
        verify(exactly = 0) { reservationJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería listar reservas paginadas con filtros combinados")
    fun `getAllReservations should return paginated reservations with combined filters`() {
        // Given
        val criteria =
            ReservationSearchCriteria(
                userId = 1L,
                status = ReservationStatus.PENDING,
                from = LocalDate.of(2026, 11, 1),
                to = LocalDate.of(2026, 11, 30),
            )
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(sampleEntity()), pageable, 1)

        // When
        every {
            reservationJpaRepository.findAll(any<Specification<ReservationEntity>>(), any<Pageable>())
        } returns page

        val (reservations, total) = reservationService.getAllReservations(criteria, 0, 10)

        // Then
        assertEquals(1, reservations.size)
        assertEquals(1L, total)
        assertEquals("res-1", reservations.first().id)
        verify { reservationJpaRepository.findAll(any<Specification<ReservationEntity>>(), any<Pageable>()) }
    }
}
