package com.lgzarturo.springbootcourse.features.payments

data class PaymentSearchCriteria(
    val reservationId: String? = null,
    val status: PaymentStatus? = null,
)
