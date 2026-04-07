package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room

data class Hotel(
    val id: String,
    val name: String,
    val address: String,
    val rooms: List<Room> = emptyList(),
)
