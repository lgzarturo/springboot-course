package com.lgzarturo.springbootcourse.features.payments

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Implementación simulada del gateway de pagos.
 * Nunca recibe ni persiste credenciales: solo importes y referencias opacas.
 */
@Component
class SimulatedPaymentGateway : PaymentGateway {
    override fun charge(request: GatewayChargeRequest): GatewayChargeResult {
        val approved = request.amount > 0 && !request.simulateFailure
        return GatewayChargeResult(approved = approved, transactionId = "sim-${UUID.randomUUID()}")
    }

    override fun refund(request: GatewayRefundRequest): GatewayRefundResult =
        GatewayRefundResult(refunded = true, transactionId = request.transactionId)
}
