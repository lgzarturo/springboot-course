package com.lgzarturo.springbootcourse.rooms.adapters.persistence

import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity

interface RoomJpaRepository {
    fun findByHotelIdAndId(
        hotelId: String,
        roomId: String,
    ): RoomEntity?
}
