package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.rooms.Room

class HotelService(
    private val hotelDomainRepository: HotelDomainRepository,
) {
    fun createHotel(hotel: Hotel): Hotel = hotelDomainRepository.save(hotel)

    fun getHotelById(hotelId: String): Hotel? = hotelDomainRepository.findById(hotelId)

    fun getAllHotels(
        criteria: HotelSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Hotel>, Long> = hotelDomainRepository.findAll(criteria, page, size)

    fun updateHotel(
        hotelId: String,
        hotel: Hotel,
    ): Hotel? {
        val existing = hotelDomainRepository.findById(hotelId) ?: return null
        val updated = hotel.copy(id = hotelId)
        return hotelDomainRepository.update(updated)
    }

    fun deleteHotel(hotelId: String): Boolean = hotelDomainRepository.deleteById(hotelId)

    fun getRoomById(
        hotelId: String,
        roomId: String,
    ): Room? = hotelDomainRepository.findRoomById(hotelId, roomId)
}
