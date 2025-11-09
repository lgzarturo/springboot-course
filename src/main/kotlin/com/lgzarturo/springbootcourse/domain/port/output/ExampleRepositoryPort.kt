package com.lgzarturo.springbootcourse.domain.port.output

import com.lgzarturo.springbootcourse.domain.model.Example
import com.lgzarturo.springbootcourse.infrastructure.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.infrastructure.rest.dto.request.ExampleRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ExampleRepositoryPort {
    fun save(example: Example): Example

    fun findById(id: Long): Example?

    fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example>

    fun update(
        id: Long,
        example: ExampleRequest,
    ): Example

    fun delete(id: Long)

    fun patch(
        id: Long,
        update: ExamplePatchUpdate,
    ): Example
}
