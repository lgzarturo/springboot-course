package com.lgzarturo.springbootcourse.features.rooms

import com.lgzarturo.springbootcourse.features.hotels.HotelEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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
