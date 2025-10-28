package com.lgzarturo.springbootcourse.infrastructure.rest.dto.request

import com.lgzarturo.springbootcourse.domain.model.Example

data class ExampleRequest(
    val name: String,
    val description: String?
) {
    fun toDomain() = Example(name = name, description = description)
}
