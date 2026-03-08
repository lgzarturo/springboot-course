package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import java.util.Optional

interface HotelJpaRepository {
    fun save(hotel: HotelEntity): HotelEntity

    fun findById(id: String): Optional<HotelEntity>

    fun existsById(id: String): Boolean

    fun deleteById(id: String)
}
