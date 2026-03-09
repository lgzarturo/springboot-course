package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.RoomJpaRepository
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity
import com.lgzarturo.springbootcourse.rooms.domain.Room

import org.springframework.stereotype.Component

@Component
class HotelRoomJpaRepository(
    private val hotelJpaRepository: HotelJpaRepository,
    private val roomJpaRepository: RoomJpaRepository,
) {
    fun save(hotel: Hotel): Hotel {
        val entity = hotel.toEntity()
        val savedEntity = hotelJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    fun findById(id: String): Hotel? {
        return hotelJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    fun update(hotel: Hotel): Hotel? {
        if (!hotelJpaRepository.existsById(hotel.id)) return null
        val entity = hotel.toEntity()
        val savedEntity = hotelJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    fun deleteById(id: String): Boolean {
        if (!hotelJpaRepository.existsById(id)) return false
        hotelJpaRepository.deleteById(id)
        return true
    }

    fun findRoomById(
        hotelId: String,
        roomId: String,
    ): Room? {
        return roomJpaRepository.findByHotelIdAndId(hotelId, roomId)?.toDomain(hotelId)
    }

    private fun Hotel.toEntity(): HotelEntity =
        HotelEntity(id, name, address, rooms.map { it.toEntity() })

    private fun HotelEntity.toDomain(): Hotel =
        Hotel(id ?: "", name, address, rooms.map { it.toDomain(id ?: "") })

    private fun Room.toEntity(): RoomEntity =
        RoomEntity(id, number, type, price, null)

    private fun RoomEntity.toDomain(hotelId: String): Room =
        Room(id, number, type, price, hotelId)
}
