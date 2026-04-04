package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yaml"])
class HotelJpaRepositoryIntegrationTest {
    @Autowired
    private lateinit var hotelJpaRepository: HotelJpaRepository

    @Test
    @DisplayName("Debería guardar un hotel en la base de datos")
    fun `save should persist hotel to database`() {
        // Given
        val entity = HotelEntity(name = "Test Hotel", address = "Test Address")

        // When
        val savedEntity = hotelJpaRepository.save(entity)

        // Then
        assertNotNull(savedEntity.id)
        assertEquals("Test Hotel", savedEntity.name)
        assertEquals("Test Address", savedEntity.address)

        // Verifica que se haya guardado correctamente
        val foundEntity = hotelJpaRepository.findById(savedEntity.id!!)
        assertTrue(foundEntity.isPresent)
        assertEquals("Test Hotel", foundEntity.get().name)
    }

    @Test
    @DisplayName("Debería encontrar un hotel por ID")
    fun `findById should return entity from database`() {
        // Given: Guarda un hotel primero
        val entityToSave = HotelEntity(name = "Find Test Hotel", address = "Find Test Address")
        val savedEntity = hotelJpaRepository.save(entityToSave)

        // When
        val foundEntity = hotelJpaRepository.findById(savedEntity.id!!)

        // Then
        assertTrue(foundEntity.isPresent)
        assertEquals("Find Test Hotel", foundEntity.get().name)
    }
}
