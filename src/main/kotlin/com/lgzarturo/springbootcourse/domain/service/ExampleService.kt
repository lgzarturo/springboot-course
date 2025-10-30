package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.input.ExampleUseCase
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort
import org.springframework.stereotype.Service

@Service
class ExampleService(
    private val repository: ExampleRepositoryPort,
) : ExampleUseCase {
    override fun create(example: Example): Example = repository.save(example)
    override fun findById(id: Long): Example = repository.findById(id)?: throw NoSuchElementException("Example with id $id not found")
}
