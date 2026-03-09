package com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity

import jakarta.persistence.*
import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity

@Entity
@Table(name = "hotels")
data class HotelEntity(
    @Id
    val id: String?,
    val name: String,
    val address: String,
    @OneToMany(mappedBy = "hotel", cascade = [CascadeType.ALL], orphanRemoval = true)
    val rooms: List<RoomEntity>,
)
