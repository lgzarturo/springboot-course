package com.lgzarturo.springbootcourse.features.examples.dto

import com.lgzarturo.springbootcourse.features.examples.Example
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Datos para crear o reemplazar un ejemplo de TDD")
data class ExampleRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 1, max = 100, message = "Name must be between {min} and {max} characters")
    @field:Schema(
        description = "Nombre del ejemplo",
        minLength = 1,
        maxLength = 100,
        example = "Bulbasaur",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:Size(max = 500, message = "Description must be less than {max} characters")
    @field:Pattern(
        regexp = "^(?!\\s*$).+",
        message = "Description cannot contain only whitespace",
    )
    @field:Schema(
        description = "Descripción opcional sin solo espacios",
        maxLength = 500,
        pattern = "^(?!\\s*$).+",
        example = "Pokémon semilla de tipo planta",
    )
    val description: String?,
) {
    fun toDomain() =
        Example(
            name = name,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
        )
}
