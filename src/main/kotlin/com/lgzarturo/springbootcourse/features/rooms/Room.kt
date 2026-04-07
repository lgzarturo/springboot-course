package com.lgzarturo.springbootcourse.features.rooms

data class Room(
    val id: String,
    val number: String,
    val type: String,
    val price: Double,
    val hotelId: String,
)
