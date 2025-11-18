package com.lgzarturo.springbootcourse.example.service

import com.lgzarturo.springbootcourse.example.application.ports.input.ExampleUseCase
import com.lgzarturo.springbootcourse.example.application.ports.output.ExampleRepositoryPort
import com.lgzarturo.springbootcourse.example.domain.Example
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ExampleService(
    private val repository: ExampleRepositoryPort,
) : ExampleUseCase {
    override fun create(example: Example): Example = repository.save(example)

    override fun update(
        id: Long,
        example: Example,
    ): Example {
        val existingExample = findById(id)
        return repository.save(existingExample.copy(name = example.name, description = example.description))
    }

    override fun findById(id: Long): Example =
        repository.findById(id) ?: throw NoSuchElementException("Example with id $id not found")

    override fun delete(id: Long) {
        repository.findById(id) ?: throw NoSuchElementException("Example with id $id not found")
        repository.delete(id)
    }

    override fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example> = repository.findAll(searchText, pageable)

    override fun patch(
        id: Long,
        update: ExamplePatchUpdate,
    ): Example {
        val existingExample = findById(id)
        return when (update.property) {
            "name" -> repository.save(existingExample.copy(name = update.value))
            "description" -> repository.save(existingExample.copy(description = update.value))
            else -> throw NoSuchElementException("Property ${update.property} not found")
        }
    }
}
