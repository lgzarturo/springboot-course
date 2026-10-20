package com.lgzarturo.springbootcourse.features.rooms

data class Room(
    val id: String,
    val number: String,
    val type: String,
    val price: Double,
    val hotelId: String,
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val theme: PokemonTheme = PokemonTheme.PIKACHU,
    val capacity: Int = 2,
    val description: String? = null,
)
