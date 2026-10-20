package com.lgzarturo.springbootcourse.features.payments.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.features.payments.Payment
import com.lgzarturo.springbootcourse.features.payments.PaymentStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de un pago")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PaymentResponse(
    @field:Schema(description = "Identificador del pago", example = "pay-1")
    val id: String? = null,
    @field:Schema(description = "Identificador de la reserva", example = "res-1")
    val reservationId: String = "",
    @field:Schema(description = "Importe cobrado (igual al total de la reserva)", example = "450.0")
    val amount: Double = 0.0,
    @field:Schema(description = "Estado del pago", example = "COMPLETED")
    val status: PaymentStatus = PaymentStatus.PENDING,
    @field:Schema(description = "Etiqueta del método simulado", example = "SIMULATED")
    val method: String = "",
    @field:Schema(description = "Referencia opaca del gateway", example = "sim-1")
    val gatewayReference: String = "",
) {
    companion object {
        fun fromDomain(payment: Payment): PaymentResponse =
            PaymentResponse(
                id = payment.id.ifEmpty { null },
                reservationId = payment.reservationId,
                amount = payment.amount,
                status = payment.status,
                method = payment.method,
                gatewayReference = payment.gatewayReference,
            )
    }
}
