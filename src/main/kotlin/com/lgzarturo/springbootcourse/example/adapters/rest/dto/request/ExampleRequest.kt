package com.lgzarturo.springbootcourse.example.adapters.rest.dto.request

import com.lgzarturo.springbootcourse.example.domain.Example
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ExampleRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between {min} and {max} characters")
    val name: String,
    @field:Size(max = 500, message = "Description must be less than {max} characters")
    @field:Pattern(
        regexp = "^(?!\\s*$).+",
        message = "Description cannot contain only whitespace",
    )
    val description: String?,
) {
    fun toDomain() =
        Example(
            name = name,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
        )
}
