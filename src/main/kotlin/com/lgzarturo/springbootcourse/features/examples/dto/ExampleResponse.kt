package com.lgzarturo.springbootcourse.features.examples.dto

import com.lgzarturo.springbootcourse.features.examples.Example
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de un ejemplo de TDD")
data class ExampleResponse(
    @field:Schema(description = "Identificador del ejemplo", example = "1")
    val id: Long?,
    @field:Schema(description = "Nombre del ejemplo", example = "Bulbasaur")
    val name: String,
    @field:Schema(
        description = "Descripción del ejemplo, si fue registrada",
        example = "Pokémon semilla de tipo planta",
    )
    val description: String?,
) {
    companion object {
        fun fromDomain(example: Example) = ExampleResponse(example.id, example.name!!, example.description)
    }
}
