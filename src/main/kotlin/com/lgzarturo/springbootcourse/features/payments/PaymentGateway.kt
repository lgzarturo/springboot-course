package com.lgzarturo.springbootcourse.features.payments

data class GatewayChargeRequest(
    val amount: Double,
    val reference: String,
    val simulateFailure: Boolean = false,
)

data class GatewayChargeResult(
    val approved: Boolean,
    val transactionId: String,
)

data class GatewayRefundRequest(
    val transactionId: String,
    val amount: Double,
)

data class GatewayRefundResult(
    val refunded: Boolean,
    val transactionId: String,
)

interface PaymentGateway {
    fun charge(request: GatewayChargeRequest): GatewayChargeResult

    fun refund(request: GatewayRefundRequest): GatewayRefundResult
}
