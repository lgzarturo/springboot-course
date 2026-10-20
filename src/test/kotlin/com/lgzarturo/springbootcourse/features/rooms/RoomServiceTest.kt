package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.hotels.HotelJpaRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

/**
 * Tests unitarios para RoomService.
 * Verifica la lógica de negocio de habitaciones sin contexto Spring.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("RoomService Tests")
class RoomServiceTest {
    @MockK
    private lateinit var roomJpaRepository: RoomJpaRepository

    @MockK
    private lateinit var hotelJpaRepository: HotelJpaRepository

    private lateinit var roomService: RoomService

    private val hotelId = "hotel-1"
    private val hotelEntity = HotelEntity(id = hotelId, name = "Gran Hotel Pikachu", address = "Calle Trueno 25")

    private fun sampleRoom(id: String = "room-1") =
        Room(
            id = id,
            number = "101",
            type = "SUITE",
            price = 150.0,
            hotelId = hotelId,
            status = RoomStatus.AVAILABLE,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            description = "Suite con vista al lago",
        )

    private fun sampleEntity(id: String? = "room-1") =
        RoomEntity(
            id = id,
            number = "101",
            type = "SUITE",
            price = 150.0,
            hotel = hotelEntity,
            status = RoomStatus.AVAILABLE,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            description = "Suite con vista al lago",
        )

    @BeforeEach
    fun setUp() {
        roomService = RoomService(roomJpaRepository, hotelJpaRepository)
    }

    @Test
    @DisplayName("Debería crear una habitación cuando el hotel existe")
    fun `createRoom should save and return the room when hotel exists`() {
        // Given
        val roomToSave = sampleRoom(id = "")
        val savedEntity = sampleEntity()

        // When
        every { hotelJpaRepository.findById(hotelId) } returns Optional.of(hotelEntity)
        every { roomJpaRepository.save(any()) } returns savedEntity

        val result = roomService.createRoom(hotelId, roomToSave)

        // Then
        assertEquals("room-1", result.id)
        assertEquals("101", result.number)
        assertEquals(RoomStatus.AVAILABLE, result.status)
        assertEquals(PokemonTheme.PIKACHU, result.theme)
        assertEquals(2, result.capacity)
        verify { hotelJpaRepository.findById(hotelId) }
        verify { roomJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar la creación cuando el hotel no existe")
    fun `createRoom should throw when hotel does not exist`() {
        // Given
        val roomToSave = sampleRoom(id = "")

        // When
        every { hotelJpaRepository.findById(hotelId) } returns Optional.empty()

        // Then
        assertThrows<IllegalArgumentException> { roomService.createRoom(hotelId, roomToSave) }
        verify { hotelJpaRepository.findById(hotelId) }
        verify(exactly = 0) { roomJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería obtener una habitación por ID dentro del hotel")
    fun `getRoomById should return the room when found`() {
        // Given
        every { roomJpaRepository.findByHotelIdAndId(hotelId, "room-1") } returns sampleEntity()

        // When
        val result = roomService.getRoomById(hotelId, "room-1")

        // Then
        assertEquals(sampleRoom(), result)
        verify { roomJpaRepository.findByHotelIdAndId(hotelId, "room-1") }
    }

    @Test
    @DisplayName("Debería retornar null cuando la habitación es de otro hotel")
    fun `getRoomById should return null when room belongs to another hotel`() {
        // Given
        every { roomJpaRepository.findByHotelIdAndId("other-hotel", "room-1") } returns null

        // When
        val result = roomService.getRoomById("other-hotel", "room-1")

        // Then
        assertNull(result)
        verify { roomJpaRepository.findByHotelIdAndId("other-hotel", "room-1") }
    }

    @Test
    @DisplayName("Debería listar habitaciones paginadas sin filtros")
    fun `getAllRooms should return paginated rooms without filters`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(sampleEntity()), pageable, 1)

        // When
        every { roomJpaRepository.findByHotelId(hotelId, any()) } returns page

        val (rooms, total) = roomService.getAllRooms(hotelId, RoomSearchCriteria(), 0, 10)

        // Then
        assertEquals(1, rooms.size)
        assertEquals(1L, total)
        assertEquals(sampleRoom(), rooms.first())
        verify { roomJpaRepository.findByHotelId(hotelId, any()) }
    }

    @Test
    @DisplayName("Debería filtrar por estado y tema Pokemon")
    fun `getAllRooms should filter by status and theme`() {
        // Given
        val criteria = RoomSearchCriteria(status = RoomStatus.AVAILABLE, theme = PokemonTheme.PIKACHU)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(sampleEntity()), pageable, 1)

        // When
        every {
            roomJpaRepository.findByHotelIdAndStatusAndTheme(hotelId, RoomStatus.AVAILABLE, PokemonTheme.PIKACHU, any())
        } returns page

        val (rooms, total) = roomService.getAllRooms(hotelId, criteria, 0, 10)

        // Then
        assertEquals(1, rooms.size)
        assertEquals(1L, total)
        verify {
            roomJpaRepository.findByHotelIdAndStatusAndTheme(hotelId, RoomStatus.AVAILABLE, PokemonTheme.PIKACHU, any())
        }
    }

    @Test
    @DisplayName("Debería filtrar solo por estado cuando no hay tema")
    fun `getAllRooms should filter only by status when theme is null`() {
        // Given
        val criteria = RoomSearchCriteria(status = RoomStatus.OCCUPIED)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(emptyList<RoomEntity>(), pageable, 0)

        // When
        every { roomJpaRepository.findByHotelIdAndStatus(hotelId, RoomStatus.OCCUPIED, any()) } returns page

        val (rooms, total) = roomService.getAllRooms(hotelId, criteria, 0, 10)

        // Then
        assertTrue(rooms.isEmpty())
        assertEquals(0L, total)
        verify { roomJpaRepository.findByHotelIdAndStatus(hotelId, RoomStatus.OCCUPIED, any()) }
    }

    @Test
    @DisplayName("Debería actualizar una habitación existente")
    fun `updateRoom should return updated room when found`() {
        // Given
        val roomUpdate = sampleRoom().copy(number = "102", capacity = 4)
        val updatedEntity = sampleEntity().copy(number = "102", capacity = 4)

        // When
        every { roomJpaRepository.findByHotelIdAndId(hotelId, "room-1") } returns sampleEntity()
        every { roomJpaRepository.save(any()) } returns updatedEntity

        val result = roomService.updateRoom(hotelId, "room-1", roomUpdate)

        // Then
        assertEquals("102", result?.number)
        assertEquals(4, result?.capacity)
        verify { roomJpaRepository.findByHotelIdAndId(hotelId, "room-1") }
        verify { roomJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería retornar null al actualizar una habitación inexistente")
    fun `updateRoom should return null when room does not exist`() {
        // Given
        every { roomJpaRepository.findByHotelIdAndId(hotelId, "missing") } returns null

        // When
        val result = roomService.updateRoom(hotelId, "missing", sampleRoom(id = "missing"))

        // Then
        assertNull(result)
        verify(exactly = 0) { roomJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería eliminar una habitación existente")
    fun `deleteRoom should return true when room exists`() {
        // Given
        every { roomJpaRepository.findByHotelIdAndId(hotelId, "room-1") } returns sampleEntity()
        every { roomJpaRepository.delete(any()) } returns Unit

        // When
        val result = roomService.deleteRoom(hotelId, "room-1")

        // Then
        assertTrue(result)
        verify { roomJpaRepository.delete(any()) }
    }

    @Test
    @DisplayName("Debería retornar false al eliminar una habitación inexistente")
    fun `deleteRoom should return false when room does not exist`() {
        // Given
        every { roomJpaRepository.findByHotelIdAndId(hotelId, "missing") } returns null

        // When
        val result = roomService.deleteRoom(hotelId, "missing")

        // Then
        assertFalse(result)
        verify(exactly = 0) { roomJpaRepository.delete(any()) }
    }
}
