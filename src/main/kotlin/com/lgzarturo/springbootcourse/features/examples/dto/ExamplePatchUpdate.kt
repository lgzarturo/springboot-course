package com.lgzarturo.springbootcourse.features.examples.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Actualización parcial de un campo del ejemplo")
data class ExamplePatchUpdate(
    @field:NotBlank(message = "Property is required")
    @field:Schema(
        description = "Nombre de la propiedad a actualizar",
        example = "description",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val property: String,
    @field:NotBlank(message = "Value is required")
    @field:Schema(
        description = "Nuevo valor de la propiedad",
        example = "Pokémon semilla de tipo planta",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val value: String,
)
