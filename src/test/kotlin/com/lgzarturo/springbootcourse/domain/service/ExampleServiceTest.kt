package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class ExampleServiceTest {

    private val repository = mock<ExampleRepositoryPort>()
    private val service = ExampleService(repository)

    @Test
    fun `should create a new example`() {
        val example = Example(id = null, name = "Test", description = "desc")

        whenever(repository.save(any())).thenReturn(example.copy(id = 1))

        val result = service.create(example)

        assertEquals(1, result.id)
        verify(repository).save(any())
    }
}
