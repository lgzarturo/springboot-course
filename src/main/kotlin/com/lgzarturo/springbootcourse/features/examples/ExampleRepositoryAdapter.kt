package com.lgzarturo.springbootcourse.features.examples

import com.lgzarturo.springbootcourse.common.pagination.PageRequest
import com.lgzarturo.springbootcourse.common.pagination.PageResult
import com.lgzarturo.springbootcourse.features.examples.dto.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.features.examples.dto.ExampleRequest
import org.springframework.data.domain.ExampleMatcher
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
        pageRequest: PageRequest,
    ): PageResult<Example> {
        val pageable = pageRequest.toPageable()

        if (searchText.isNullOrBlank()) {
            return PageResult.fromPage(jpaRepository.findAll(pageable).map { it.toDomain() })
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

        return PageResult.fromPage(
            jpaRepository
                .findAll(example, pageable)
                .map { it.toDomain() },
        )
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
