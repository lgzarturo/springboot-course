package com.lgzarturo.springbootcourse.domain.port.output

import com.lgzarturo.springbootcourse.domain.model.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ExampleRepositoryPort {
    fun save(example: Example): Example

    fun findById(id: Long): Example?

    fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example>
}
