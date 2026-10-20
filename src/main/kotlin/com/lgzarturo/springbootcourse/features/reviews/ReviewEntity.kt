package com.lgzarturo.springbootcourse.features.reviews

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "reviews")
data class ReviewEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    @Column(name = "reservation_id", unique = true)
    val reservationId: String = "",
    @Column(name = "hotel_id")
    val hotelId: String = "",
    @Column(name = "user_id")
    val userId: Long = 0,
    @Column(name = "rating")
    val rating: Int = 0,
    @Column(name = "comment")
    val comment: String = "",
)
