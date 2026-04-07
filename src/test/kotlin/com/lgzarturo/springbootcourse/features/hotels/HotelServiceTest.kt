package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests unitarios para HotelService
 * Verifica la lógica de negocio del servicio de Hotel
 */
@ExtendWith(MockKExtension::class)
@DisplayName("HotelService Tests")
class HotelServiceTest {
    @MockK
    private lateinit var hotelRepositoryPort: HotelRepositoryPort
    private lateinit var hotelService: HotelService

    @BeforeEach
    fun setUp() {
        hotelService = HotelService(hotelRepositoryPort)
    }

    @Test
    @DisplayName("Debería crear un hotel correctamente")
    fun `createHotel should save and return the hotel`() {
        // Given
        val hotelToSave = Hotel("1", "Test Hotel", "Test Address", emptyList())
        val savedHotel = Hotel("1", "Test Hotel", "Test Address", emptyList())

        // When
        every { hotelRepositoryPort.save(hotelToSave) } returns savedHotel

        val result = hotelService.createHotel(hotelToSave)

        // Then
        Assertions.assertEquals(savedHotel, result)
        verify { hotelRepositoryPort.save(hotelToSave) }
    }

    @Test
    @DisplayName("Debería obtener un hotel por ID correctamente")
    fun `getHotelById should return the hotel if found`() {
        // Given
        val hotelId = "1"
        val expectedHotel = Hotel("1", "Test Hotel", "Test Address", emptyList())

        // When
        every { hotelRepositoryPort.findById(hotelId) } returns expectedHotel

        val result = hotelService.getHotelById(hotelId)

        // Then
        Assertions.assertEquals(expectedHotel, result)
        verify { hotelRepositoryPort.findById(hotelId) }
    }

    @Test
    @DisplayName("Debería retornar null si el hotel no es encontrado")
    fun `getHotelById should return null if hotel is not found`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelRepositoryPort.findById(hotelId) } returns null

        val result = hotelService.getHotelById(hotelId)

        // Then
        assertNull(result)
        verify { hotelRepositoryPort.findById(hotelId) }
    }

    @Test
    @DisplayName("Debería obtener todos los hoteles con criterios de búsqueda")
    fun `getAllHotels should return a list of hotels and total count`() {
        // Given
        val criteria = HotelSearchCriteria(name = "Test")
        val page = 0
        val size = 10
        val hotels =
            listOf(
                Hotel("1", "Test Hotel 1", "Address 1", emptyList()),
                Hotel("2", "Test Hotel 2", "Address 2", emptyList()),
            )
        val total = 2L

        // When
        every { hotelRepositoryPort.findAll(criteria, page, size) } returns Pair(hotels, total)

        val (resultHotels, resultTotal) = hotelService.getAllHotels(criteria, page, size)

        // Then
        Assertions.assertEquals(hotels, resultHotels)
        Assertions.assertEquals(total, resultTotal)
        verify { hotelRepositoryPort.findAll(criteria, page, size) }
    }

    @Test
    @DisplayName("Debería actualizar un hotel existente")
    fun `updateHotel should return updated hotel if found`() {
        // Given
        val hotelId = "1"
        val existingHotel = Hotel("1", "Old Name", "Old Address", emptyList())
        val hotelToUpdate = Hotel("1", "Updated Name", "Updated Address", emptyList())
        val updatedHotel = Hotel("1", "Updated Name", "Updated Address", emptyList())

        // When
        every { hotelRepositoryPort.findById(hotelId) } returns existingHotel
        every { hotelRepositoryPort.update(any()) } returns updatedHotel

        val result = hotelService.updateHotel(hotelId, hotelToUpdate)

        // Then
        Assertions.assertEquals(updatedHotel, result)
        verify { hotelRepositoryPort.findById(hotelId) }
        verify { hotelRepositoryPort.update(any()) }
    }

    @Test
    @DisplayName("Debería retornar null si el hotel no se encuentra")
    fun `updateHotel should return null if hotel is not found`() {
        // Given
        val hotelId = "non-existent-id"
        val hotelToUpdate = Hotel("non-existent-id", "Updated Name", "Updated Address", emptyList())

        // When
        every { hotelRepositoryPort.findById(hotelId) } returns null

        val result = hotelService.updateHotel(hotelId, hotelToUpdate)

        // Then
        assertNull(result)
        verify { hotelRepositoryPort.findById(hotelId) }
        verify(exactly = 0) { hotelRepositoryPort.update(any()) }
    }

    @Test
    @DisplayName("Debería eliminar un hotel existente")
    fun `deleteHotel should return true if deletion is successful`() {
        // Given
        val hotelId = "1"

        // When
        every { hotelRepositoryPort.deleteById(hotelId) } returns true

        val result = hotelService.deleteHotel(hotelId)

        // Then
        Assertions.assertTrue(result)
        verify { hotelRepositoryPort.deleteById(hotelId) }
    }

    @Test
    @DisplayName("Debería retornar false si el hotel no se encuentra")
    fun `deleteHotel should return false if hotel does not exist`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelRepositoryPort.deleteById(hotelId) } returns false

        val result = hotelService.deleteHotel(hotelId)

        // Then
        Assertions.assertFalse(result)
        verify { hotelRepositoryPort.deleteById(hotelId) }
    }

    @Test
    @DisplayName("Debería retornar con el cuarto de un hotel")
    fun `getRoomById should return the room if found`() {
        // Given
        val hotelId = "1"
        val roomId = "101"
        val expectedRoom = Room("101", "101", "Standard", 100.0, hotelId)

        // When
        every { hotelRepositoryPort.findRoomById(hotelId, roomId) } returns expectedRoom

        val result = hotelService.getRoomById(hotelId, roomId)

        // Then
        Assertions.assertEquals(expectedRoom, result)
        verify { hotelRepositoryPort.findRoomById(hotelId, roomId) }
    }

    @Test
    @DisplayName("Debería retornar null si el cuarto no se encuentra")
    fun `getRoomById should return null if room is not found`() {
        // Given
        val hotelId = "1"
        val roomId = "non-existent-room-id"

        // When
        every { hotelRepositoryPort.findRoomById(hotelId, roomId) } returns null

        val result = hotelService.getRoomById(hotelId, roomId)

        // Then
        assertNull(result)
        verify { hotelRepositoryPort.findRoomById(hotelId, roomId) }
    }
}
