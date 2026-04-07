package com.lgzarturo.springbootcourse.features.examples

import com.lgzarturo.springbootcourse.common.pagination.PageRequest
import com.lgzarturo.springbootcourse.common.pagination.PageResult
import com.lgzarturo.springbootcourse.features.examples.dto.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.features.examples.dto.ExampleRequest

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
