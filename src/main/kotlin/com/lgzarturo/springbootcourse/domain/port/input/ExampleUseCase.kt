package com.lgzarturo.springbootcourse.domain.port.input

import com.lgzarturo.springbootcourse.domain.model.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ExampleUseCase {
    fun create(example: Example): Example

    fun update(
        id: Long,
        example: Example,
    ): Example

    fun findById(id: Long): Example

    fun delete(id: Long)

    fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example>

    fun patch(
        id: Long,
        example: Example,
    ): Example
}
