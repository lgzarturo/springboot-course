package com.lgzarturo.springbootcourse.features.payments

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "payments")
data class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    @Column(name = "reservation_id")
    val reservationId: String = "",
    @Column(name = "amount")
    val amount: Double = 0.0,
    @Enumerated(EnumType.STRING)
    val status: PaymentStatus = PaymentStatus.PENDING,
    @Column(name = "method")
    val method: String = "SIMULATED",
    @Column(name = "gateway_reference")
    val gatewayReference: String = "",
)
