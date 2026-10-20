package com.lgzarturo.springbootcourse.common.pagination

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "Resultado paginado con metadatos")
data class PageResult<T>(
    @field:Schema(description = "Elementos de la página actual")
    val items: List<T>,
    @field:Schema(description = "Total de elementos", example = "42")
    val total: Long,
    @field:Schema(description = "Número de página actual (base 0)", example = "0")
    val page: Int,
    @field:Schema(description = "Tamaño de página", example = "10")
    val size: Int,
    @field:Schema(description = "Indica si la página está vacía", example = "false")
    val empty: Boolean = items.isEmpty(),
    @field:Schema(description = "Total de páginas", example = "5")
    val pages: Int = if (size == 0) 0 else ((total + size - 1) / size).toInt(),
) {
    companion object {
        fun <T : Any> fromPage(page: Page<T>): PageResult<T> =
            PageResult(
                items = page.content,
                total = page.totalElements,
                page = page.number,
                size = page.size,
                empty = page.isEmpty,
                pages = page.totalPages,
            )
    }
}
