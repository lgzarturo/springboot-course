package com.lgzarturo.springbootcourse.features.hotels

data class HotelSearchCriteria(
    val name: String? = null,
    val address: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
)
