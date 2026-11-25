package com.lgzarturo.springbootcourse.common.pagination

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

@Schema(description = "Criterio de paginación y ordenamiento del dominio")
data class PageRequest(
    @field:Schema(description = "Número de página (base 0)", example = "0")
    val page: Int,
    @field:Schema(description = "Tamaño de página", example = "10")
    val size: Int,
    @field:Schema(description = "Ordenamientos por propiedad")
    val sort: List<SortOrder> = emptyList(),
) {
    fun toPageable(): Pageable {
        val sortOrders =
            if (sort.isEmpty()) {
                Sort.unsorted()
            } else {
                val orders =
                    sort.map {
                        when (it.direction) {
                            SortOrder.Direction.ASC -> Sort.Order.asc(it.property)
                            SortOrder.Direction.DESC -> Sort.Order.desc(it.property)
                        }
                    }
                Sort.by(orders)
            }
        return PageRequest
            .of(page, size, sortOrders)
    }
}
