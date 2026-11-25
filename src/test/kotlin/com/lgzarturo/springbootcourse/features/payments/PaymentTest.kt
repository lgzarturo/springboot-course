package com.lgzarturo.springbootcourse.features.payments

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests unitarios del modelo de dominio Payment.
 * Verifica la máquina de estados del pago sin Spring.
 */
@DisplayName("Payment Domain Tests")
class PaymentTest {
    private fun samplePayment(status: PaymentStatus = PaymentStatus.PENDING) =
        Payment(
            id = "pay-1",
            reservationId = "res-1",
            amount = 450.0,
            status = status,
            method = "SIMULATED",
            gatewayReference = "sim-1",
        )

    @Test
    @DisplayName("Debería completar un pago pendiente")
    fun `should complete a pending payment`() {
        // Given
        val payment = samplePayment()

        // When
        val completed = payment.transitionTo(PaymentStatus.COMPLETED)

        // Then
        assertEquals(PaymentStatus.COMPLETED, completed.status)
        assertEquals(450.0, completed.amount)
        assertEquals("res-1", completed.reservationId)
    }

    @Test
    @DisplayName("Debería marcar como fallido un pago pendiente")
    fun `should fail a pending payment`() {
        // Given
        val payment = samplePayment()

        // When
        val failed = payment.transitionTo(PaymentStatus.FAILED)

        // Then
        assertEquals(PaymentStatus.FAILED, failed.status)
    }

    @Test
    @DisplayName("Debería reembolsar un pago completado")
    fun `should refund a completed payment`() {
        // Given
        val payment = samplePayment(status = PaymentStatus.COMPLETED)

        // When
        val refunded = payment.transitionTo(PaymentStatus.REFUNDED)

        // Then
        assertEquals(PaymentStatus.REFUNDED, refunded.status)
    }

    @Test
    @DisplayName("Debería exponer la matriz completa de transiciones válidas")
    fun `should expose the full transition matrix`() {
        // Given
        val pending = samplePayment(status = PaymentStatus.PENDING)
        val completed = samplePayment(status = PaymentStatus.COMPLETED)
        val failed = samplePayment(status = PaymentStatus.FAILED)
        val refunded = samplePayment(status = PaymentStatus.REFUNDED)

        // When / Then
        assertTrue(pending.canTransitionTo(PaymentStatus.COMPLETED))
        assertTrue(pending.canTransitionTo(PaymentStatus.FAILED))
        assertFalse(pending.canTransitionTo(PaymentStatus.PENDING))
        assertFalse(pending.canTransitionTo(PaymentStatus.REFUNDED))
        assertTrue(completed.canTransitionTo(PaymentStatus.REFUNDED))
        assertFalse(completed.canTransitionTo(PaymentStatus.COMPLETED))
        assertFalse(completed.canTransitionTo(PaymentStatus.FAILED))
        assertFalse(failed.canTransitionTo(PaymentStatus.COMPLETED))
        assertFalse(failed.canTransitionTo(PaymentStatus.REFUNDED))
        assertFalse(refunded.canTransitionTo(PaymentStatus.COMPLETED))
    }

    @Test
    @DisplayName("Debería rechazar transiciones inválidas y estados finales")
    fun `should reject invalid transitions and terminal states`() {
        // Given
        val pending = samplePayment(status = PaymentStatus.PENDING)
        val completed = samplePayment(status = PaymentStatus.COMPLETED)
        val failed = samplePayment(status = PaymentStatus.FAILED)
        val refunded = samplePayment(status = PaymentStatus.REFUNDED)

        // When / Then
        assertThrows<IllegalStateException> { pending.transitionTo(PaymentStatus.REFUNDED) }
        assertThrows<IllegalStateException> { completed.transitionTo(PaymentStatus.FAILED) }
        assertThrows<IllegalStateException> { completed.transitionTo(PaymentStatus.COMPLETED) }
        assertThrows<IllegalStateException> { failed.transitionTo(PaymentStatus.COMPLETED) }
        assertThrows<IllegalStateException> { failed.transitionTo(PaymentStatus.REFUNDED) }
        assertThrows<IllegalStateException> { refunded.transitionTo(PaymentStatus.COMPLETED) }
    }
}
