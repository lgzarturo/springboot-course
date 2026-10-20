package com.lgzarturo.springbootcourse.features.payments

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Tests de persistencia para PaymentJpaRepository.
 * Verifica el mapeo de la entidad y las consultas por reserva.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = ["classpath:application-test.yaml"])
@DisplayName("PaymentJpaRepository Tests")
class PaymentJpaRepositoryTest {
    @Autowired
    private lateinit var paymentJpaRepository: PaymentJpaRepository

    private fun paymentEntity(reservationId: String = "res-1") =
        PaymentEntity(
            reservationId = reservationId,
            amount = 450.0,
            status = PaymentStatus.COMPLETED,
            method = "SIMULATED",
            gatewayReference = "sim-1",
        )

    @Test
    @DisplayName("Debería persistir todos los campos del pago")
    fun `save should persist all payment fields`() {
        // When
        val saved = paymentJpaRepository.save(paymentEntity())

        // Then
        assertNotNull(saved.id)
        val found = paymentJpaRepository.findById(saved.id!!).orElse(null)
        assertNotNull(found)
        assertEquals("res-1", found?.reservationId)
        assertEquals(450.0, found?.amount)
        assertEquals(PaymentStatus.COMPLETED, found?.status)
        assertEquals("SIMULATED", found?.method)
        assertEquals("sim-1", found?.gatewayReference)
    }

    @Test
    @DisplayName("Debería buscar pagos por reserva")
    fun `findByReservationId should return payments for the reservation`() {
        // Given
        paymentJpaRepository.save(paymentEntity("res-1"))
        paymentJpaRepository.save(paymentEntity("res-1").copy(status = PaymentStatus.FAILED))
        paymentJpaRepository.save(paymentEntity("res-2"))

        // When
        val found = paymentJpaRepository.findByReservationId("res-1")

        // Then
        assertEquals(2, found.size)
        assertTrue(found.all { it.reservationId == "res-1" })
    }

    @Test
    @DisplayName("Debería devolver vacío para reservas sin pagos")
    fun `findByReservationId should return empty for unknown reservations`() {
        // When
        val found = paymentJpaRepository.findByReservationId("missing")

        // Then
        assertTrue(found.isEmpty())
    }
}
