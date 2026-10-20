package com.lgzarturo.springbootcourse.features.rooms.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Página de resultados con metadatos de paginación")
data class PageResponse<T>(
    @field:Schema(description = "Elementos de la página actual")
    val content: List<T> = emptyList(),
    @field:Schema(description = "Total de elementos en todas las páginas", example = "42")
    val totalElements: Long = 0,
    @field:Schema(description = "Total de páginas disponibles", example = "5")
    val totalPages: Int = 0,
    @field:Schema(description = "Indica si es la primera página", example = "true")
    val first: Boolean = false,
    @field:Schema(description = "Indica si es la última página", example = "false")
    val last: Boolean = false,
    @field:Schema(description = "Número de página actual (base 0)", example = "0")
    val number: Int = 0,
    @field:Schema(description = "Tamaño de página solicitado", example = "10")
    val size: Int = 0,
) {
    companion object {
        fun <T> from(
            items: List<T>,
            total: Long,
            page: Int,
            size: Int,
        ): PageResponse<T> {
            val totalPages = if (size > 0) ((total + size - 1) / size).toInt() else 0
            return PageResponse(
                content = items,
                totalElements = total,
                totalPages = totalPages,
                first = page == 0,
                last = page >= totalPages - 1 || totalPages == 0,
                number = page,
                size = size,
            )
        }
    }
}
