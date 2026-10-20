package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "reservations")
data class ReservationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    val room: RoomEntity? = null,
    @Column(name = "user_id")
    val userId: Long = 0,
    @Column(name = "guest_name")
    val guestName: String = "",
    @Column(name = "check_in")
    val checkIn: LocalDate = LocalDate.now(),
    @Column(name = "check_out")
    val checkOut: LocalDate = LocalDate.now().plusDays(1),
    @Column(name = "total_price")
    val totalPrice: Double = 0.0,
    @Enumerated(EnumType.STRING)
    val status: ReservationStatus = ReservationStatus.PENDING,
)
