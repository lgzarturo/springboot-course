package com.lgzarturo.springbootcourse.features.hotels.integration

import com.lgzarturo.springbootcourse.features.hotels.Hotel
import com.lgzarturo.springbootcourse.features.hotels.HotelRoomJpaRepository
import com.lgzarturo.springbootcourse.features.hotels.HotelSearchCriteria
import com.lgzarturo.springbootcourse.features.rooms.Room
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Tests de integración para HotelRoomJpaRepository
 * Verifica el adaptador de persistencia contra una base de datos H2 real
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yaml"])
@Import(HotelRoomJpaRepository::class)
@DisplayName("HotelRoomJpaRepository Integration Tests")
class HotelRoomJpaRepositoryIntegrationTest {
    @Autowired
    private lateinit var repository: HotelRoomJpaRepository

    @Test
    @DisplayName("Debería guardar un hotel con habitaciones y recuperarlo por id")
    fun `should save hotel with rooms and find by id`() {
        // Given
        val hotel =
            Hotel(
                id = "",
                name = "Hotel Pokemon",
                address = "Ciudad Paleta",
                rooms = listOf(Room(id = "", number = "101", type = "Standard", price = 100.0, hotelId = "")),
            )

        // When
        val saved = repository.save(hotel)
        val found = repository.findById(saved.id)

        // Then
        assertNotNull(found)
        assertEquals("Hotel Pokemon", found?.name)
        assertEquals(1, found?.rooms?.size)
        assertEquals("101", found?.rooms?.first()?.number)
        assertEquals(saved.id, found?.rooms?.first()?.hotelId)
    }

    @Test
    @DisplayName("Debería retornar null al buscar un hotel inexistente")
    fun `should return null when hotel not found`() {
        val found = repository.findById("no-existe")

        assertNull(found)
    }

    @Test
    @DisplayName("Debería filtrar hoteles por nombre")
    fun `should filter hotels by name`() {
        // Given
        repository.save(Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()))
        repository.save(Hotel(id = "", name = "Hotel Digimon", address = "Otra ciudad", rooms = emptyList()))

        // When
        val (hotels, total) = repository.findAll(HotelSearchCriteria(name = "pokemon"), page = 0, size = 10)

        // Then
        assertEquals(1, hotels.size)
        assertEquals(1, total)
        assertEquals("Hotel Pokemon", hotels.first().name)
    }

    @Test
    @DisplayName("Debería filtrar hoteles por dirección")
    fun `should filter hotels by address`() {
        // Given
        repository.save(Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()))
        repository.save(Hotel(id = "", name = "Hotel Digimon", address = "Otra ciudad", rooms = emptyList()))

        // When
        val (hotels, total) = repository.findAll(HotelSearchCriteria(address = "paleta"), page = 0, size = 10)

        // Then
        assertEquals(1, hotels.size)
        assertEquals(1, total)
        assertEquals("Hotel Pokemon", hotels.first().name)
    }

    @Test
    @DisplayName("Debería listar todos los hoteles sin criterios")
    fun `should list all hotels without criteria`() {
        // Given
        repository.save(Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()))
        repository.save(Hotel(id = "", name = "Hotel Digimon", address = "Otra ciudad", rooms = emptyList()))

        // When
        val (hotels, total) = repository.findAll(HotelSearchCriteria(), page = 0, size = 10)

        // Then
        assertEquals(2, hotels.size)
        assertEquals(2, total)
    }

    @Test
    @DisplayName("Debería actualizar un hotel existente")
    fun `should update existing hotel`() {
        // Given
        val saved =
            repository.save(
                Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()),
            )

        // When
        val updated = repository.update(saved.copy(name = "Hotel Pokemon Renovado"))

        // Then
        assertNotNull(updated)
        assertEquals("Hotel Pokemon Renovado", updated?.name)
    }

    @Test
    @DisplayName("Debería retornar null al actualizar un hotel inexistente")
    fun `should return null when updating non-existent hotel`() {
        val updated = repository.update(Hotel(id = "no-existe", name = "X", address = "Y", rooms = emptyList()))

        assertNull(updated)
    }

    @Test
    @DisplayName("Debería eliminar un hotel existente")
    fun `should delete existing hotel`() {
        // Given
        val saved =
            repository.save(
                Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()),
            )

        // When
        val deleted = repository.deleteById(saved.id)

        // Then
        assertTrue(deleted)
        assertNull(repository.findById(saved.id))
    }

    @Test
    @DisplayName("Debería retornar false al eliminar un hotel inexistente")
    fun `should return false when deleting non-existent hotel`() {
        val deleted = repository.deleteById("no-existe")

        assertFalse(deleted)
    }

    @Test
    @DisplayName("Debería encontrar una habitación por hotel y room id")
    fun `should find room by hotel and room id`() {
        // Given
        val hotel =
            Hotel(
                id = "",
                name = "Hotel Pokemon",
                address = "Ciudad Paleta",
                rooms = listOf(Room(id = "", number = "101", type = "Standard", price = 100.0, hotelId = "")),
            )
        val saved = repository.save(hotel)
        val roomId =
            repository
                .findById(saved.id)
                ?.rooms
                ?.first()
                ?.id

        // When
        val room = repository.findRoomById(saved.id, roomId!!)

        // Then
        assertNotNull(room)
        assertEquals("101", room?.number)
        assertEquals(saved.id, room?.hotelId)
    }

    @Test
    @DisplayName("Debería retornar null al buscar una habitación inexistente")
    fun `should return null when room not found`() {
        val saved =
            repository.save(
                Hotel(id = "", name = "Hotel Pokemon", address = "Ciudad Paleta", rooms = emptyList()),
            )

        val room = repository.findRoomById(saved.id, "no-existe")

        assertNull(room)
    }
}
