package com.lgzarturo.springbootcourse.domain.service

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.domain.port.output.ExampleRepositoryPort

class ExampleService(
    private val repository: ExampleRepositoryPort,
) {
    fun create(example: Example): Example = repository.save(example)
}
