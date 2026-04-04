package com.lgzarturo.springbootcourse.hotels.service

import com.lgzarturo.springbootcourse.hotels.application.ports.output.HotelRepositoryPort
import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.hotels.domain.HotelSearchCriteria
import com.lgzarturo.springbootcourse.rooms.domain.Room

class HotelService(
    private val hotelRepositoryPort: HotelRepositoryPort,
) {
    fun createHotel(hotel: Hotel): Hotel = hotelRepositoryPort.save(hotel)

    fun getHotelById(hotelId: String): Hotel? = hotelRepositoryPort.findById(hotelId)

    fun getAllHotels(
        criteria: HotelSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Hotel>, Long> = hotelRepositoryPort.findAll(criteria, page, size)

    fun updateHotel(
        hotelId: String,
        hotel: Hotel,
    ): Hotel? {
        val existing = hotelRepositoryPort.findById(hotelId) ?: return null
        val updated = hotel.copy(id = hotelId)
        return hotelRepositoryPort.update(updated)
    }

    fun deleteHotel(hotelId: String): Boolean = hotelRepositoryPort.deleteById(hotelId)

    fun getRoomById(
        hotelId: String,
        roomId: String,
    ): Room? = hotelRepositoryPort.findRoomById(hotelId, roomId)
}
