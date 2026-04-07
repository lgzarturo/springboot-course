package com.lgzarturo.springbootcourse.features.examples.dto

import jakarta.validation.constraints.NotBlank

data class ExamplePatchUpdate(
    @field:NotBlank(message = "Property is required")
    val property: String,
    @field:NotBlank(message = "Value is required")
    val value: String,
)
