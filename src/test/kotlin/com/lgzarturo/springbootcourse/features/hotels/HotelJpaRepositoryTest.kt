package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room
import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class HotelJpaRepositoryTest {
    @MockK
    private lateinit var hotelJpaRepository: HotelJpaRepository

    @MockK
    private lateinit var roomJpaRepository: RoomJpaRepository

    private lateinit var jpaHotelRepository: HotelRoomJpaRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        jpaHotelRepository = HotelRoomJpaRepository(hotelJpaRepository, roomJpaRepository)
    }

    @Test
    fun `save should map domain to entity, save, and return mapped domain`() {
        // Given
        val domainHotel = Hotel("1", "Test Hotel", "Test Address", emptyList())
        val savedEntity = HotelEntity("1", "Test Hotel", "Test Address", emptyList())
        val expectedDomain = Hotel("1", "Test Hotel", "Test Address", emptyList())

        // When
        every { hotelJpaRepository.save(any()) } returns savedEntity

        val result = jpaHotelRepository.save(domainHotel)

        // Then
        Assertions.assertEquals(expectedDomain, result)
        verify {
            hotelJpaRepository.save(
                withArg<HotelEntity> { entity ->
                    assert(entity.id == "1")
                    assert(entity.name == "Test Hotel")
                },
            )
        }
    }

    @Test
    fun `findById should return mapped domain hotel if found`() {
        // Given
        val hotelId = "1"
        val entity = HotelEntity("1", "Test Hotel", "Test Address", emptyList())
        val expectedDomain = Hotel("1", "Test Hotel", "Test Address", emptyList())

        // When
        every { hotelJpaRepository.findByIdWithRooms(hotelId) } returns Optional.of(entity)

        val result = jpaHotelRepository.findById(hotelId)

        // Then
        Assertions.assertEquals(expectedDomain, result)
        verify { hotelJpaRepository.findByIdWithRooms(hotelId) }
    }

    @Test
    fun `findById should return null if entity not found`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelJpaRepository.findByIdWithRooms(hotelId) } returns Optional.empty()

        val result = jpaHotelRepository.findById(hotelId)

        // Then
        Assertions.assertNull(result)
        verify { hotelJpaRepository.findByIdWithRooms(hotelId) }
    }

    @Test
    fun `update should map domain to entity, save if exists, and return mapped domain`() {
        // Given
        val domainHotel = Hotel("1", "Updated Name", "Updated Address", emptyList())
        val savedEntity = HotelEntity("1", "Updated Name", "Updated Address", emptyList())
        val expectedDomain = Hotel("1", "Updated Name", "Updated Address", emptyList())

        // When
        every { hotelJpaRepository.existsById("1") } returns true
        every { hotelJpaRepository.save(any()) } returns savedEntity

        val result = jpaHotelRepository.update(domainHotel)

        // Then
        Assertions.assertEquals(expectedDomain, result)
        verify { hotelJpaRepository.existsById("1") }
        verify {
            hotelJpaRepository.save(
                withArg<HotelEntity> { entity ->
                    assert(entity.id == "1")
                    assert(entity.name == "Updated Name")
                },
            )
        }
    }

    @Test
    fun `update should return null if entity does not exist`() {
        // Given
        val domainHotel = Hotel("non-existent-id", "Updated Name", "Updated Address", emptyList())

        // When
        every { hotelJpaRepository.existsById("non-existent-id") } returns false

        val result = jpaHotelRepository.update(domainHotel)

        // Then
        Assertions.assertNull(result)
        verify { hotelJpaRepository.existsById("non-existent-id") }
        verify(exactly = 0) { hotelJpaRepository.save(any()) }
    }

    @Test
    fun `deleteById should return true if deletion is successful`() {
        // Given
        val hotelId = "1"

        // When
        every { hotelJpaRepository.existsById(hotelId) } returns true
        every { hotelJpaRepository.deleteById(hotelId) } returns Unit

        val result = jpaHotelRepository.deleteById(hotelId)

        // Then
        Assertions.assertTrue(result)
        verify { hotelJpaRepository.existsById(hotelId) }
        verify { hotelJpaRepository.deleteById(hotelId) }
    }

    @Test
    fun `deleteById should return false if entity does not exist`() {
        // Given
        val hotelId = "non-existent-id"

        // When
        every { hotelJpaRepository.existsById(hotelId) } returns false

        val result = jpaHotelRepository.deleteById(hotelId)

        // Then
        Assertions.assertFalse(result)
        verify { hotelJpaRepository.existsById(hotelId) }
        verify(exactly = 0) { hotelJpaRepository.deleteById(any()) }
    }

    @Test
    fun `findRoomById should return mapped domain room if found`() {
        // Given
        val hotelId = "1"
        val roomId = "101"
        val entity = RoomEntity("101", "101", "Standard", 100.0, null)
        val expectedDomain = Room("101", "101", "Standard", 100.0, hotelId)

        // When
        every { roomJpaRepository.findByHotelIdAndId(hotelId, roomId) } returns entity

        val result = jpaHotelRepository.findRoomById(hotelId, roomId)

        // Then
        Assertions.assertEquals(expectedDomain, result)
        verify { roomJpaRepository.findByHotelIdAndId(hotelId, roomId) }
    }

    @Test
    fun `findRoomById should return null if room entity not found`() {
        // Given
        val hotelId = "1"
        val roomId = "non-existent-room-id"

        // When
        every { roomJpaRepository.findByHotelIdAndId(hotelId, roomId) } returns null

        val result = jpaHotelRepository.findRoomById(hotelId, roomId)

        // Then
        Assertions.assertNull(result)
        verify { roomJpaRepository.findByHotelIdAndId(hotelId, roomId) }
    }
}
