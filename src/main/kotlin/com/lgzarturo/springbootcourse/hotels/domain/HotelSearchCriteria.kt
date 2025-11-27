package com.lgzarturo.springbootcourse.hotels.domain

data class HotelSearchCriteria(
    val name: String? = null,
    val address: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
)
