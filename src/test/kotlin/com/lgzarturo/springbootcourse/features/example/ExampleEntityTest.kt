package com.lgzarturo.springbootcourse.features.example

import com.lgzarturo.springbootcourse.features.examples.Example
import com.lgzarturo.springbootcourse.features.examples.ExampleEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * ExampleEntity Tests
 * Verifica los mapeos entre la entidad JPA y el dominio
 */
@DisplayName("ExampleEntity Tests")
class ExampleEntityTest {
    @Test
    @DisplayName("Debería mapear una entidad a dominio")
    fun `should map entity to domain`() {
        val entity = ExampleEntity(id = 1, name = "Test", description = "Desc")

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("Test", domain.name)
        assertEquals("Desc", domain.description)
    }

    @Test
    @DisplayName("Debería mapear una entidad con descripción nula a dominio")
    fun `should map entity with null description to domain`() {
        val entity = ExampleEntity(id = 2, name = "Test", description = null)

        val domain = entity.toDomain()

        assertEquals(2L, domain.id)
        assertNull(domain.description)
    }

    @Test
    @DisplayName("Debería mapear un dominio a entidad")
    fun `should map domain to entity`() {
        val domain = Example(id = 1, name = "Test", description = "Desc")

        val entity = ExampleEntity.fromDomain(domain)

        assertEquals(1L, entity.id)
        assertEquals("Test", entity.name)
        assertEquals("Desc", entity.description)
        assertNotNull(entity.createdAt)
        assertNotNull(entity.updatedAt)
    }

    @Test
    @DisplayName("Debería mapear un dominio sin id a entidad")
    fun `should map domain without id to entity`() {
        val domain = Example(id = null, name = "Test", description = null)

        val entity = ExampleEntity.fromDomain(domain)

        assertNull(entity.id)
        assertEquals("Test", entity.name)
        assertNull(entity.description)
    }
}
