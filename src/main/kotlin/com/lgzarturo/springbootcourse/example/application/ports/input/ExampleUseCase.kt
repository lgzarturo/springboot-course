package com.lgzarturo.springbootcourse.example.application.ports.input

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.domain.Example
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
        update: ExamplePatchUpdate,
    ): Example
}
