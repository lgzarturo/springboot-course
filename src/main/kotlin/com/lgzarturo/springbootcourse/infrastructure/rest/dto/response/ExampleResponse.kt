package com.lgzarturo.springbootcourse.infrastructure.rest.dto.response

import com.lgzarturo.springbootcourse.domain.model.Example

data class ExampleResponse(
    val id: Long?,
    val name: String,
    val description: String?,
) {
    companion object {
        fun fromDomain(example: Example) = ExampleResponse(example.id, example.name!!, example.description)
    }
}
