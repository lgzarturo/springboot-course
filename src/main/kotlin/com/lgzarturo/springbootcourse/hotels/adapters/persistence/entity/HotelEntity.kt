package com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity

data class HotelEntity(
    val id: String?,
    val name: String,
    val address: String,
    val rooms: List<Any>,
)
