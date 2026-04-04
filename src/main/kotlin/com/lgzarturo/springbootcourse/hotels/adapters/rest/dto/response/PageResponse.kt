package com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val first: Boolean = false,
    val last: Boolean = false,
    val number: Int = 0,
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
