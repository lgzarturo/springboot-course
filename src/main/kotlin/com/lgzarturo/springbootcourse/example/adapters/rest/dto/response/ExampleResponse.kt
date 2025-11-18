package com.lgzarturo.springbootcourse.example.adapters.rest.dto.response

import com.lgzarturo.springbootcourse.example.domain.Example

data class ExampleResponse(
    val id: Long?,
    val name: String,
    val description: String?,
) {
    companion object {
        fun fromDomain(example: Example) = ExampleResponse(example.id, example.name!!, example.description)
    }
}
