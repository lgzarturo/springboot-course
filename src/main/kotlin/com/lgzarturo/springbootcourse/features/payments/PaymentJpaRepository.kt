package com.lgzarturo.springbootcourse.features.payments

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface PaymentJpaRepository :
    JpaRepository<PaymentEntity, String>,
    JpaSpecificationExecutor<PaymentEntity> {
    fun findByReservationId(reservationId: String): List<PaymentEntity>
}
