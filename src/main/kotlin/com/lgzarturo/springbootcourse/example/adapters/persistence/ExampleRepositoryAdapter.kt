package com.lgzarturo.springbootcourse.example.adapters.persistence

import com.lgzarturo.springbootcourse.example.adapters.persistence.entity.ExampleEntity
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.example.application.ports.output.ExampleRepositoryPort
import com.lgzarturo.springbootcourse.example.domain.Example
import org.springframework.data.domain.ExampleMatcher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

/**
 * Repository para manejar la capa de persistencia de los datos de ejemplo.
 */
@Repository
class ExampleRepositoryAdapter(
    private val jpaRepository: ExampleJpaRepository,
) : ExampleRepositoryPort {
    override fun save(example: Example): Example {
        val entity = ExampleEntity.fromDomain(example)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): Example? = jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example> {
        if (searchText.isNullOrBlank()) {
            return jpaRepository.findAll(pageable).map { it.toDomain() }
        }

        val matcher =
            ExampleMatcher
                .matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains())
                .withIgnorePaths("id", "createdAt", "updatedAt", "description")

        val exampleEntity =
            ExampleEntity(
                name = searchText,
                description = null,
            )

        val example =
            org.springframework.data.domain.Example
                .of(exampleEntity, matcher)

        return jpaRepository
            .findAll(example, pageable)
            .map { it.toDomain() }
    }

    override fun update(
        id: Long,
        example: ExampleRequest,
    ): Example =
        jpaRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Example with id $id not found") }
            .copy(
                name = example.name,
                description = example.description,
            ).let { jpaRepository.save(it).toDomain() }

    override fun delete(id: Long) {
        jpaRepository.deleteById(id)
    }

    override fun patch(
        id: Long,
        update: ExamplePatchUpdate,
    ): Example {
        val existingExample =
            jpaRepository
                .findById(
                    id,
                ).orElseThrow { NoSuchElementException("Example with id $id not found") }
        val updatedExample =
            when (update.property) {
                "name" -> existingExample.copy(name = update.value)
                "description" -> existingExample.copy(description = update.value)
                else -> throw NoSuchElementException("Property ${update.property} not found")
            }
        return jpaRepository.save(updatedExample).toDomain()
    }
}
