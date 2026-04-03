package com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToOne
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn

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
