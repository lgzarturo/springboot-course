package com.lgzarturo.springbootcourse.features.examples.dto

import com.lgzarturo.springbootcourse.features.examples.Example

data class ExampleResponse(
    val id: Long?,
    val name: String,
    val description: String?,
) {
    companion object {
        fun fromDomain(example: Example) = ExampleResponse(example.id, example.name!!, example.description)
    }
}
