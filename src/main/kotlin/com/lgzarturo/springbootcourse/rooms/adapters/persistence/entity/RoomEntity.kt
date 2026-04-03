package com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity

import jakarta.persistence.*
import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity

@Entity
@Table(name = "rooms")
data class RoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,
    val number: String,
    val type: String,
    val price: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    val hotel: HotelEntity? = null,
)
