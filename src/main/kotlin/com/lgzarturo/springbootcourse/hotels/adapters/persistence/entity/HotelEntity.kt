package com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity

import com.lgzarturo.springbootcourse.rooms.adapters.persistence.entity.RoomEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "hotels")
data class HotelEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    val name: String,
    val address: String,
    @OneToMany(mappedBy = "hotel", cascade = [CascadeType.ALL], orphanRemoval = true)
    val rooms: List<RoomEntity> = emptyList(),
)
