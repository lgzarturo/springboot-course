package com.lgzarturo.springbootcourse.common.pagination

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Ordenamiento por una propiedad")
data class SortOrder(
    @field:Schema(description = "Propiedad por la que ordenar", example = "name")
    val property: String,
    @field:Schema(description = "Dirección del ordenamiento", allowableValues = ["ASC", "DESC"], example = "ASC")
    val direction: Direction,
) {
    enum class Direction { ASC, DESC }
}
