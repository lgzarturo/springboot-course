package com.lgzarturo.springbootcourse.example.application.ports.output

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExampleRequest
import com.lgzarturo.springbootcourse.example.domain.Example
import com.lgzarturo.springbootcourse.shared.domain.PageRequest
import com.lgzarturo.springbootcourse.shared.domain.PageResult

interface ExampleRepositoryPort {
    fun save(example: Example): Example

    fun findById(id: Long): Example?

    fun findAll(
        searchText: String?,
        pageRequest: PageRequest,
    ): PageResult<Example>

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
