package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@TestPropertySource(locations = ["classpath:application-tests.yaml"])
class HotelJpaRepositoryIntegrationTest {
    @Autowired
    private lateinit var hotelJpaRepository: HotelJpaRepository

    @Test
    fun `save should persist hotel to database`() {
        // Given
        val entity = HotelEntity("1", "Test Hotel", "Test Address", emptyList())

        // When
        val savedEntity = hotelJpaRepository.save(entity)

        // Then
        assertNotNull(savedEntity.id)
        assertEquals("Test Hotel", savedEntity.name)
        assertEquals("Test Address", savedEntity.address)

        // Verifica que se haya guardado correctamente
        val foundEntity = hotelJpaRepository.findById("1")
        assertTrue(foundEntity.isPresent)
        assertEquals("Test Hotel", foundEntity.get().name)
    }

    @Test
    fun `findById should return entity from database`() {
        // Given: Guarda un hotel primero
        val entityToSave = HotelEntity("2", "Find Test Hotel", "Find Test Address", emptyList())
        hotelJpaRepository.save(entityToSave)

        // When
        val foundEntity = hotelJpaRepository.findById("2")

        // Then
        assertTrue(foundEntity.isPresent)
        assertEquals("Find Test Hotel", foundEntity.get().name)
    }
}
