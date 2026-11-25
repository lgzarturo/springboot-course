package com.lgzarturo.springbootcourse.features.payments

data class Payment(
    val id: String,
    val reservationId: String,
    val amount: Double,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val method: String = "SIMULATED",
    val gatewayReference: String = "",
) {
    fun canTransitionTo(target: PaymentStatus): Boolean = VALID_TRANSITIONS[status].orEmpty().contains(target)

    fun transitionTo(target: PaymentStatus): Payment {
        check(canTransitionTo(target)) {
            "Transición inválida de $status a $target"
        }
        return copy(status = target)
    }

    companion object {
        private val VALID_TRANSITIONS: Map<PaymentStatus, Set<PaymentStatus>> =
            mapOf(
                PaymentStatus.PENDING to setOf(PaymentStatus.COMPLETED, PaymentStatus.FAILED),
                PaymentStatus.COMPLETED to setOf(PaymentStatus.REFUNDED),
                PaymentStatus.FAILED to emptySet(),
                PaymentStatus.REFUNDED to emptySet(),
            )
    }
}
