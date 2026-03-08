package com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity

data class RoomEntity(
    val id: String,
    val number: String,
    val type: String,
    val price: Double,
    val hotel: HotelEntity? = null,
)
