package com.lgzarturo.springbootcourse.features.examples

import com.lgzarturo.springbootcourse.common.pagination.PageRequest
import com.lgzarturo.springbootcourse.common.pagination.PageResult
import com.lgzarturo.springbootcourse.features.examples.dto.ExamplePatchUpdate

class ExampleServicePort(
    private val repository: ExampleRepositoryPort,
) : ExampleUseCasePort {
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
        pageable: PageRequest,
    ): PageResult<Example> = repository.findAll(searchText, pageable)

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
