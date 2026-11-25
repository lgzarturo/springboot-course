package com.lgzarturo.springbootcourse.features.rooms

data class RoomSearchCriteria(
    val status: RoomStatus? = null,
    val theme: PokemonTheme? = null,
)
