package com.lgzarturo.springbootcourse.features.rooms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomJpaRepository : JpaRepository<RoomEntity, String> {
    fun findByHotelIdAndId(
        hotelId: String,
        roomId: String,
    ): RoomEntity?
}
