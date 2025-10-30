package com.lgzarturo.springbootcourse.domain.port.output

import com.lgzarturo.springbootcourse.domain.model.Example

interface ExampleRepositoryPort {
    fun save(example: Example): Example
    fun findById(id: Long): Example?
}
