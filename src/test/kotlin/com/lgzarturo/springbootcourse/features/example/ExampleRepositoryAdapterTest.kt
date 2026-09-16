package com.lgzarturo.springbootcourse.features.example

import com.lgzarturo.springbootcourse.common.pagination.PageRequest
import com.lgzarturo.springbootcourse.features.examples.Example
import com.lgzarturo.springbootcourse.features.examples.ExampleEntity
import com.lgzarturo.springbootcourse.features.examples.ExampleJpaRepository
import com.lgzarturo.springbootcourse.features.examples.ExampleRepositoryAdapter
import com.lgzarturo.springbootcourse.features.examples.dto.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.features.examples.dto.ExampleRequest
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Tests unitarios para ExampleRepositoryAdapter
 * Verifica el adaptador de persistencia con el repositorio JPA mockeado
 */
@DisplayName("ExampleRepositoryAdapter Tests")
class ExampleRepositoryAdapterTest {
    private val jpaRepository = mockk<ExampleJpaRepository>()
    private val adapter = ExampleRepositoryAdapter(jpaRepository)

    @Test
    @DisplayName("Debería guardar un ejemplo y retornar el dominio")
    fun `should save example and return domain`() {
        val example = Example(id = null, name = "Test", description = "Desc")
        val savedEntity = ExampleEntity(id = 1, name = "Test", description = "Desc")

        every { jpaRepository.save(any()) } returns savedEntity

        val result = adapter.save(example)

        assertEquals(1L, result.id)
        assertEquals("Test", result.name)
        assertEquals("Desc", result.description)
        verify { jpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería encontrar un ejemplo por id")
    fun `should find example by id`() {
        val entity = ExampleEntity(id = 1, name = "Test", description = "Desc")

        every { jpaRepository.findById(1L) } returns Optional.of(entity)

        val result = adapter.findById(1L)

        assertEquals(1L, result?.id)
        assertEquals("Test", result?.name)
        verify { jpaRepository.findById(1L) }
    }

    @Test
    @DisplayName("Debería retornar null cuando el ejemplo no existe")
    fun `should return null when example not found`() {
        every { jpaRepository.findById(999L) } returns Optional.empty()

        val result = adapter.findById(999L)

        assertNull(result)
        verify { jpaRepository.findById(999L) }
    }

    @Test
    @DisplayName("Debería listar todos los ejemplos cuando searchText es nulo")
    fun `should find all when searchText is null`() {
        val entities = listOf(ExampleEntity(id = 1, name = "Alpha", description = null))
        val page = PageImpl(entities)

        every { jpaRepository.findAll(any<Pageable>()) } returns page

        val result = adapter.findAll(null, PageRequest(page = 0, size = 10))

        assertEquals(1, result.items.size)
        assertEquals("Alpha", result.items[0].name)
        assertEquals(1, result.total)
        verify { jpaRepository.findAll(any<Pageable>()) }
    }

    @Test
    @DisplayName("Debería listar todos los ejemplos cuando searchText está en blanco")
    fun `should find all when searchText is blank`() {
        val entities = listOf(ExampleEntity(id = 1, name = "Alpha", description = null))
        val page = PageImpl(entities)

        every { jpaRepository.findAll(any<Pageable>()) } returns page

        val result = adapter.findAll("   ", PageRequest(page = 0, size = 10))

        assertEquals(1, result.items.size)
        verify { jpaRepository.findAll(any<Pageable>()) }
    }

    @Test
    @DisplayName("Debería buscar ejemplos por texto cuando searchText tiene valor")
    fun `should search examples when searchText has value`() {
        val entities = listOf(ExampleEntity(id = 2, name = "Beta", description = "B"))
        val page = PageImpl(entities)

        every {
            jpaRepository.findAll(any<org.springframework.data.domain.Example<ExampleEntity>>(), any<Pageable>())
        } returns page

        val result = adapter.findAll("Beta", PageRequest(page = 0, size = 10))

        assertEquals(1, result.items.size)
        assertEquals("Beta", result.items[0].name)
        verify {
            jpaRepository.findAll(any<org.springframework.data.domain.Example<ExampleEntity>>(), any<Pageable>())
        }
    }

    @Test
    @DisplayName("Debería actualizar un ejemplo existente")
    fun `should update existing example`() {
        val existing = ExampleEntity(id = 1, name = "Old", description = "Old")
        val request = ExampleRequest(name = "New", description = "New desc")
        val updated = ExampleEntity(id = 1, name = "New", description = "New desc")

        every { jpaRepository.findById(1L) } returns Optional.of(existing)
        every { jpaRepository.save(any()) } returns updated

        val result = adapter.update(1L, request)

        assertEquals("New", result.name)
        assertEquals("New desc", result.description)
        verify { jpaRepository.findById(1L) }
        verify { jpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar un ejemplo inexistente")
    fun `should throw when updating non-existent example`() {
        every { jpaRepository.findById(999L) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            adapter.update(999L, ExampleRequest(name = "New", description = "New desc"))
        }
    }

    @Test
    @DisplayName("Debería eliminar un ejemplo por id")
    fun `should delete example by id`() {
        every { jpaRepository.deleteById(1L) } just runs

        adapter.delete(1L)

        verify { jpaRepository.deleteById(1L) }
    }

    @Test
    @DisplayName("Debería actualizar parcialmente el nombre de un ejemplo")
    fun `should patch example name`() {
        val existing = ExampleEntity(id = 1, name = "Old", description = "Desc")
        val updated = ExampleEntity(id = 1, name = "New Name", description = "Desc")

        every { jpaRepository.findById(1L) } returns Optional.of(existing)
        every { jpaRepository.save(any()) } returns updated

        val result = adapter.patch(1L, ExamplePatchUpdate(property = "name", value = "New Name"))

        assertEquals("New Name", result.name)
        verify { jpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería actualizar parcialmente la descripción de un ejemplo")
    fun `should patch example description`() {
        val existing = ExampleEntity(id = 1, name = "Name", description = "Old")
        val updated = ExampleEntity(id = 1, name = "Name", description = "New Desc")

        every { jpaRepository.findById(1L) } returns Optional.of(existing)
        every { jpaRepository.save(any()) } returns updated

        val result = adapter.patch(1L, ExamplePatchUpdate(property = "description", value = "New Desc"))

        assertEquals("New Desc", result.description)
        verify { jpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar parcialmente una propiedad inválida")
    fun `should throw when patching invalid property`() {
        val existing = ExampleEntity(id = 1, name = "Name", description = "Desc")

        every { jpaRepository.findById(1L) } returns Optional.of(existing)

        assertThrows<NoSuchElementException> {
            adapter.patch(1L, ExamplePatchUpdate(property = "unknown", value = "value"))
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar parcialmente un ejemplo inexistente")
    fun `should throw when patching non-existent example`() {
        every { jpaRepository.findById(999L) } returns Optional.empty()

        assertThrows<NoSuchElementException> {
            adapter.patch(999L, ExamplePatchUpdate(property = "name", value = "value"))
        }
    }
}
