package com.lgzarturo.springbootcourse.rooms.adapters.persistence

import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomJpaRepository : JpaRepository<RoomEntity, String> {
    fun findByHotelIdAndId(
        hotelId: String,
        roomId: String,
    ): RoomEntity?
}
