package com.lgzarturo.springbootcourse.example.service

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.application.ports.output.ExampleRepositoryPort
import com.lgzarturo.springbootcourse.example.domain.Example
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class ExampleServiceTest {
    private val repository = mockk<ExampleRepositoryPort>()
    private val service = ExampleServicePort(repository)

    @Test
    @DisplayName("Debería crear un nuevo ejemplo")
    fun `should create a new example`() {
        val example = Example(id = null, name = "Test", description = "desc")

        every { repository.save(any()) } returns example.copy(id = 1)

        val result = service.create(example)

        Assertions.assertEquals(1, result.id)
        verify { repository.save(any()) }
    }

    @Test
    @DisplayName("Debería crear un nuevo ejemplo con descripción nula")
    fun `should create a new example with null description`() {
        val example = Example(id = null, name = "Test", description = null)

        every { repository.save(any()) } returns example.copy(id = 1)

        val result = service.create(example)
        Assertions.assertEquals(1, result.id)

        verify { repository.save(any()) }
        Assertions.assertEquals(null, result.description)
        Assertions.assertEquals("Test", result.name)
    }

    @Test
    @DisplayName("Debería poder obtener un ejemplo por su id")
    fun `should get an example by its id`() {
        val example = Example(id = 1, name = "Test", description = "desc")

        every { repository.findById(1) } returns example

        val result = service.findById(1)

        Assertions.assertEquals(example, result)
        verify { repository.findById(1) }
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException cuando el ejemplo no existe")
    fun `should throw NoSuchElementException when example does not exist`() {
        every { repository.findById(1) } returns null

        assertThrows<NoSuchElementException> {
            service.findById(1)
        }
        verify { repository.findById(1) }
    }

    @Test
    @DisplayName("Debería poder actualizar un ejemplo")
    fun `should update an example`() {
        val existing = Example(id = 1, name = "Old", description = "Old desc")
        val update = Example(id = null, name = "New", description = "New desc")
        val updated = Example(id = 1, name = "New", description = "New desc")

        every { repository.findById(1) } returns existing
        every { repository.save(any()) } returns updated

        val result = service.update(1, update)

        Assertions.assertEquals(updated, result)
        verify { repository.findById(1) }
        verify { repository.save(any()) }
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar un ejemplo inexistente")
    fun `should throw exception when updating a non-existent example`() {
        every { repository.findById(999) } returns null

        assertThrows<NoSuchElementException> {
            service.update(999, Example(id = null, name = "New", description = "New desc"))
        }
        verify { repository.findById(999) }
    }

    @Test
    @DisplayName("Debería poder eliminar un ejemplo por su id")
    fun `should delete an example by its id`() {
        val existing = Example(id = 1, name = "To Delete", description = null)
        every { repository.findById(1) } returns existing

        // Acción
        service.delete(1)

        // Verificación básica de búsqueda previa
        verify { repository.findById(1) }
        // Nota: la verificación de deleteById pertenecerá al puerto cuando exista el método
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar un ejemplo inexistente")
    fun `should throw exception when deleting a non-existent example`() {
        every { repository.findById(999) } returns null

        assertThrows<NoSuchElementException> {
            service.delete(999)
        }
        verify { repository.findById(999) }
    }

    @Test
    @DisplayName("Debería listar todos los ejemplos cuando searchText es null")
    fun `should list all examples when searchText is null`() {
        val pageable = PageRequest.of(0, 10)
        val items =
            listOf(
                Example(id = 1, name = "Alpha", description = null),
                Example(id = 2, name = "Beta", description = "B"),
            )
        val page = PageImpl(items, pageable, items.size.toLong())

        // Cuando el repositorio devuelve una página con elementos
        every { repository.findAll(isNull(), any()) } returns page

        // Entonces el servicio debe propagar la página
        val result = service.findAll(null, pageable)

        Assertions.assertEquals(page, result)
        // Verifica que se consultó con algún filtro (null) y el pageable
        verify { repository.findAll(isNull(), any()) }
    }

    @Test
    @DisplayName("Debería listar ejemplos filtrados cuando searchText tiene valor")
    fun `should list filtered examples when searchText has value`() {
        // Given
        val searchText = "Alpha"
        val pageable = PageRequest.of(0, 10)
        val items =
            listOf(
                Example(id = 1, name = "Alpha", description = null),
            )
        val expectedPage = PageImpl(items, pageable, items.size.toLong())

        // When
        every { repository.findAll(eq(searchText), eq(pageable)) } returns expectedPage

        // Then
        val result = service.findAll(searchText, pageable)

        assertNotNull(result)
        Assertions.assertEquals(1, result.content.size)
        Assertions.assertEquals("Alpha", result.content[0].name)
        verify(exactly = 1) { repository.findAll(eq(searchText), eq(pageable)) }
    }

    @Test
    @DisplayName("Debería retornar página vacía cuando no hay resultados")
    fun `should return empty page when no results found`() {
        // Given
        val searchText = "NonExistent"
        val pageable = PageRequest.of(0, 10)
        val emptyPage = PageImpl<Example>(emptyList(), pageable, 0)

        // When
        every { repository.findAll(eq(searchText), eq(pageable)) } returns emptyPage

        // Then
        val result = service.findAll(searchText, pageable)

        assertNotNull(result)
        Assertions.assertTrue(result.content.isEmpty())
        Assertions.assertEquals(0, result.totalElements)
        verify(exactly = 1) { repository.findAll(eq(searchText), eq(pageable)) }
    }

    @Test
    @DisplayName("Debería listar todos cuando searchText es string vacío")
    fun `should list all when searchText is empty string`() {
        // Given
        val pageable = PageRequest.of(0, 10)
        val items =
            listOf(
                Example(id = 1, name = "Alpha", description = null),
                Example(id = 2, name = "Beta", description = "B"),
            )
        val expectedPage = PageImpl(items, pageable, items.size.toLong())

        // When
        every { repository.findAll(eq(""), eq(pageable)) } returns expectedPage

        // Then
        val result = service.findAll("", pageable)

        assertNotNull(result)
        Assertions.assertEquals(2, result.content.size)
        verify(exactly = 1) { repository.findAll(eq(""), eq(pageable)) }
    }

    @Test
    @DisplayName("Debería devolver una lista vacía cuando no hay ejemplos")
    fun `should return an empty list when there are no examples`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(emptyList<Example>(), pageable, 0)

        every { repository.findAll(isNull(), any()) } returns page

        val result = service.findAll(null, pageable)

        Assertions.assertEquals(0, result.totalElements)
        Assertions.assertEquals(0, result.content.size)
        verify { repository.findAll(any(), any()) }
    }

    @Test
    @DisplayName("Debería buscar ejemplos por nombre")
    fun `should search examples by name`() {
        val pageable = PageRequest.of(0, 10)
        val items = listOf(Example(id = 3, name = "Test", description = "Desc"))
        val page = PageImpl(items, pageable, 1)

        every { repository.findAll(any(), any()) } returns page

        val result = service.findAll("test", pageable)

        Assertions.assertEquals(1, result.totalElements)
        Assertions.assertEquals("Test", result.content.first().name)
        verify { repository.findAll(any(), any()) }
    }

    @Test
    @DisplayName("Debería devolver una lista vacía cuando no hay ejemplos con el nombre dado")
    fun `should return an empty list when there are no examples with the given name`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(emptyList<Example>(), pageable, 0)

        every { repository.findAll(any(), any()) } returns page

        val result = service.findAll("unknown", pageable)

        Assertions.assertEquals(0, result.totalElements)
        verify { repository.findAll(any(), any()) }
    }

    @Test
    @DisplayName("Debería poder actualizar solo la descripción de un ejemplo")
    fun `should update only the description of an example`() {
        val existing = Example(id = 1, name = "Name", description = "Old")
        val patch = ExamplePatchUpdate(property = "name", value = "New")
        val updated = Example(id = 1, name = "Name", description = "New")

        every { repository.findById(1) } returns existing
        every { repository.save(any()) } returns updated

        val result = service.patch(1, patch)

        Assertions.assertEquals(updated, result)
        verify { repository.findById(1) }
        verify { repository.save(any()) }
    }

    @Test
    @DisplayName("Debería lazar excepción al actualizar la descripción de un ejemplo inexistente")
    fun `should throw exception when updating the description of a non-existent example`() {
        every { repository.findById(999) } returns null

        assertThrows<NoSuchElementException> {
            service.patch(999, ExamplePatchUpdate(property = "name", value = "New Title"))
        }
        verify { repository.findById(999) }
    }
}
