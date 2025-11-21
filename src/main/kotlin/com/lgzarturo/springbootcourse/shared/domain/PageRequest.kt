package com.lgzarturo.springbootcourse.shared.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class PageRequest(
    val page: Int,
    val size: Int,
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
        return org.springframework.data.domain.PageRequest
            .of(page, size, sortOrders)
    }
}
