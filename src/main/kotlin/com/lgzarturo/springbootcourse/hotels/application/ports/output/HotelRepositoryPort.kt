package com.lgzarturo.springbootcourse.hotels.application.ports.output

import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.hotels.domain.HotelSearchCriteria
import com.lgzarturo.springbootcourse.rooms.domain.Room

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
