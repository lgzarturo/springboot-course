package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ExampleServiceTest {
    private val repository = mock<ExampleRepositoryPort>()
    private val service = ExampleService(repository)

    @Test
    @DisplayName("Debería crear un nuevo ejemplo")
    fun `should create a new example`() {
        val example = Example(id = null, name = "Test", description = "desc")

        whenever(repository.save(any())).thenReturn(example.copy(id = 1))

        val result = service.create(example)

        assertEquals(1, result.id)
        verify(repository).save(any())
    }

    @Test
    @DisplayName("Debería poder obtener un ejemplo por su id")
    fun `should get an example by its id`() {
        val example = Example(id = 1, name = "Test", description = "desc")

        whenever(repository.findById(1)).thenReturn(example)

        val result = service.findById(1)

        assertEquals(example, result)
        verify(repository).findById(1)
    }

    @Test
    @DisplayName("Debería lanzar NoSuchElementException cuando el ejemplo no existe")
    fun `should throw NoSuchElementException when example does not exist`() {
        whenever(repository.findById(1)).thenReturn(null)

        assertThrows<NoSuchElementException> {
            service.findById(1)
        }
        verify(repository).findById(1)
    }
}
