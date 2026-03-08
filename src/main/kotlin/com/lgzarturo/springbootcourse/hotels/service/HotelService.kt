@file:Suppress("UnusedParameter", "UnusedPrivateProperty", "FunctionOnlyReturningConstant")

package com.lgzarturo.springbootcourse.hotels.service

import com.lgzarturo.springbootcourse.hotels.application.ports.output.HotelRepositoryPort
import com.lgzarturo.springbootcourse.hotels.domain.Hotel
import com.lgzarturo.springbootcourse.hotels.domain.HotelSearchCriteria

class HotelService(
    private val hotelRepositoryPort: HotelRepositoryPort,
) {
    fun createHotel(hotel: Hotel): Hotel {
        // PENDING: Implement this method
        return hotel
    }

    fun getHotelById(hotelId: String): Hotel? {
        // PENDING: Implement this method
        return null
    }

    fun getAllHotels(
        criteria: HotelSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Hotel>, Long> =
        Pair(
            listOf(),
            0,
        )

    fun updateHotel(
        hotelId: String,
        hotel: Hotel,
    ): Hotel? {
        // PENDING: Implement this method
        return null
    }

    fun deleteHotel(hotelId: String): Boolean {
        // PENDING: Implement this method
        return false
    }

    fun getRoomById(
        hotelId: String,
        roomId: String,
    ): Any? {
        // PENDING: Implement this method
        return null
    }
}
