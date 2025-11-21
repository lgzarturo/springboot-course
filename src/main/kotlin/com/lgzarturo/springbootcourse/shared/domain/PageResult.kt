package com.lgzarturo.springbootcourse.shared.domain

data class PageResult<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val empty: Boolean = items.isEmpty(),
    val pages: Int = if (size == 0) 0 else ((total + size - 1) / size).toInt(),
) {
    companion object {
        fun <T> fromPage(page: org.springframework.data.domain.Page<T>): PageResult<T> =
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
