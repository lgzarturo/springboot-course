package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import com.lgzarturo.springbootcourse.features.hotels.HotelJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Tests de persistencia para RoomJpaRepository.
 * Verifica el mapeo de la entidad y las consultas por hotel, estado y tema.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yaml"])
@DisplayName("RoomJpaRepository Tests")
class RoomJpaRepositoryTest {
    @Autowired
    private lateinit var roomJpaRepository: RoomJpaRepository

    @Autowired
    private lateinit var hotelJpaRepository: HotelJpaRepository

    private fun savedHotel(name: String = "Gran Hotel Pikachu"): HotelEntity =
        hotelJpaRepository.save(HotelEntity(name = name, address = "Calle Trueno 25"))

    private fun roomEntity(hotel: HotelEntity) =
        RoomEntity(
            number = "101",
            type = "SUITE",
            price = 150.0,
            hotel = hotel,
            status = RoomStatus.AVAILABLE,
            theme = PokemonTheme.PIKACHU,
            capacity = 2,
            description = "Suite con vista al lago",
        )

    @Test
    @DisplayName("Debería persistir tipo, estado, tema, capacidad y descripción")
    fun `save should persist all room fields`() {
        // Given
        val hotel = savedHotel()

        // When
        val saved = roomJpaRepository.save(roomEntity(hotel))

        // Then
        assertNotNull(saved.id)
        val found = roomJpaRepository.findByHotelIdAndId(hotel.id!!, saved.id!!)
        assertNotNull(found)
        assertEquals("SUITE", found?.type)
        assertEquals(RoomStatus.AVAILABLE, found?.status)
        assertEquals(PokemonTheme.PIKACHU, found?.theme)
        assertEquals(2, found?.capacity)
        assertEquals("Suite con vista al lago", found?.description)
        assertEquals(150.0, found?.price)
    }

    @Test
    @DisplayName("Debería aislar habitaciones por hotel")
    fun `findByHotelIdAndId should return null for another hotel`() {
        // Given
        val hotel = savedHotel()
        val otherHotel = savedHotel("Hotel Charizard")
        val saved = roomJpaRepository.save(roomEntity(hotel))

        // When
        val found = roomJpaRepository.findByHotelIdAndId(otherHotel.id!!, saved.id!!)

        // Then
        assertNull(found)
    }

    @Test
    @DisplayName("Debería paginar habitaciones por hotel")
    fun `findByHotelId should return paged rooms`() {
        // Given
        val hotel = savedHotel()
        roomJpaRepository.save(roomEntity(hotel).copy(number = "101"))
        roomJpaRepository.save(roomEntity(hotel).copy(number = "102"))
        roomJpaRepository.save(roomEntity(hotel).copy(number = "103"))

        // When
        val page = roomJpaRepository.findByHotelId(hotel.id!!, PageRequest.of(0, 2))

        // Then
        assertEquals(2, page.content.size)
        assertEquals(3L, page.totalElements)
    }

    @Test
    @DisplayName("Debería filtrar por estado y tema")
    fun `findByHotelIdAndStatusAndTheme should filter rooms`() {
        // Given
        val hotel = savedHotel()
        roomJpaRepository.save(roomEntity(hotel).copy(number = "101"))
        roomJpaRepository.save(
            roomEntity(hotel).copy(
                number = "102",
                status = RoomStatus.OCCUPIED,
                theme = PokemonTheme.CHARIZARD,
            ),
        )

        // When
        val page =
            roomJpaRepository.findByHotelIdAndStatusAndTheme(
                hotel.id!!,
                RoomStatus.OCCUPIED,
                PokemonTheme.CHARIZARD,
                PageRequest.of(0, 10),
            )

        // Then
        assertEquals(1L, page.totalElements)
        assertEquals("102", page.content.first().number)
    }

    @Test
    @DisplayName("Debería filtrar solo por estado")
    fun `findByHotelIdAndStatus should filter by status`() {
        // Given
        val hotel = savedHotel()
        roomJpaRepository.save(roomEntity(hotel).copy(number = "101", status = RoomStatus.MAINTENANCE))
        roomJpaRepository.save(roomEntity(hotel).copy(number = "102", status = RoomStatus.AVAILABLE))

        // When
        val page =
            roomJpaRepository.findByHotelIdAndStatus(hotel.id!!, RoomStatus.AVAILABLE, PageRequest.of(0, 10))

        // Then
        assertEquals(1L, page.totalElements)
        assertEquals("102", page.content.first().number)
    }

    @Test
    @DisplayName("Debería filtrar solo por tema")
    fun `findByHotelIdAndTheme should filter by theme`() {
        // Given
        val hotel = savedHotel()
        roomJpaRepository.save(roomEntity(hotel).copy(number = "101", theme = PokemonTheme.PIKACHU))
        roomJpaRepository.save(roomEntity(hotel).copy(number = "102", theme = PokemonTheme.MEWTWO))

        // When
        val page =
            roomJpaRepository.findByHotelIdAndTheme(hotel.id!!, PokemonTheme.MEWTWO, PageRequest.of(0, 10))

        // Then
        assertEquals(1L, page.totalElements)
        assertEquals("102", page.content.first().number)
    }
}
