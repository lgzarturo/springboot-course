package com.lgzarturo.springbootcourse.hotels.domain

import com.lgzarturo.springbootcourse.rooms.domain.Room

data class Hotel(
    val id: String,
    val name: String,
    val address: String,
    val rooms: List<Room> = emptyList(),
)
