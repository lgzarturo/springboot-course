package com.lgzarturo.springbootcourse.features.rooms

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomJpaRepository : JpaRepository<RoomEntity, String> {
    fun findByHotelIdAndId(
        hotelId: String,
        roomId: String,
    ): RoomEntity?

    fun findByHotelId(
        hotelId: String,
        pageable: Pageable,
    ): Page<RoomEntity>

    fun findByHotelIdAndStatus(
        hotelId: String,
        status: RoomStatus,
        pageable: Pageable,
    ): Page<RoomEntity>

    fun findByHotelIdAndTheme(
        hotelId: String,
        theme: PokemonTheme,
        pageable: Pageable,
    ): Page<RoomEntity>

    fun findByHotelIdAndStatusAndTheme(
        hotelId: String,
        status: RoomStatus,
        theme: PokemonTheme,
        pageable: Pageable,
    ): Page<RoomEntity>
}
