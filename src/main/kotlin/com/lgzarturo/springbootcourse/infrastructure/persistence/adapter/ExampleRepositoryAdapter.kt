package com.lgzarturo.springbootcourse.infrastructure.persistence.adapter

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort
import com.lgzarturo.springbootcourse.infrastructure.persistence.entity.ExampleEntity
import com.lgzarturo.springbootcourse.infrastructure.persistence.repository.ExampleJpaRepository
import org.springframework.stereotype.Repository

@Repository
class ExampleRepositoryAdapter(
    private val jpaRepository: ExampleJpaRepository,
) : ExampleRepositoryPort {
    override fun save(example: Example): Example {
        val entity = ExampleEntity.fromDomain(example)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findById(id: Long): Example? = jpaRepository.findById(id).orElse(null)?.toDomain()
}
