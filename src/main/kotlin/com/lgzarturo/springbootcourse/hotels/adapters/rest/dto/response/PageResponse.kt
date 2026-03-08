package com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response

data class PageResponse(
    val content: List<HotelResponse>,
    val totalElements: Int,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
)
