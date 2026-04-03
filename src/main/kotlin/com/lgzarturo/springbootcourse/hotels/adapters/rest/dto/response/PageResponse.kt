package com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val size: Int,
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
