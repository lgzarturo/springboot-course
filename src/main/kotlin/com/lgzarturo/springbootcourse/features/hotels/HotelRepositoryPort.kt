package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room

interface HotelRepositoryPort {
    fun save(hotel: Hotel): Hotel

    fun findById(id: String): Hotel?

    fun findAll(
        criteria: HotelSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Hotel>, Long>

    fun update(hotel: Hotel): Hotel?

    fun deleteById(id: String): Boolean

    fun findRoomById(
        hotelId: String,
        roomId: String,
    ): Room?
}
