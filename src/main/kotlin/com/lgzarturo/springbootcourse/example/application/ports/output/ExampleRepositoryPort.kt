package com.lgzarturo.springbootcourse.example.application.ports.output

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.example.domain.Example
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
