@file:Suppress("UnusedParameter", "FunctionOnlyReturningConstant")

package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.RoomJpaRepository
import com.lgzarturo.springbootcourse.rooms.domain.Room

class HotelRoomJpaRepository(
    val hotelJpaRepository: HotelJpaRepository,
    val roomJpaRepository: RoomJpaRepository,
) {
    fun save(hotel: Hotel): Hotel = hotel

    fun findById(id: String): Hotel = Hotel("", "", "", emptyList())

    fun update(hotel: Hotel): Hotel = hotel

    fun deleteById(id: String): Boolean = true

    fun findRoomById(
        hotelId: String,
        roomId: String,
    ): Room? = null
}
