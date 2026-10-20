package com.lgzarturo.springbootcourse.features.payments

import com.lgzarturo.springbootcourse.features.reservations.ReservationEntity
import com.lgzarturo.springbootcourse.features.reservations.ReservationJpaRepository
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.util.Optional

/**
 * Tests unitarios para PaymentService.
 * Verifica creación, duplicados, importe, reembolsos y sincronización de reserva sin Spring.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {
    @MockK
    private lateinit var paymentJpaRepository: PaymentJpaRepository

    @MockK
    private lateinit var reservationJpaRepository: ReservationJpaRepository

    @MockK
    private lateinit var paymentGateway: PaymentGateway

    private lateinit var paymentService: PaymentService

    private val checkIn = LocalDate.of(2026, 11, 1)
    private val checkOut = LocalDate.of(2026, 11, 4)

    private fun sampleReservationEntity(status: ReservationStatus = ReservationStatus.CONFIRMED) =
        ReservationEntity(
            id = "res-1",
            room = null,
            userId = 1L,
            guestName = "Ash Ketchum",
            checkIn = checkIn,
            checkOut = checkOut,
            totalPrice = 450.0,
            status = status,
        )

    private fun samplePaymentEntity(
        id: String? = "pay-1",
        status: PaymentStatus = PaymentStatus.COMPLETED,
    ) = PaymentEntity(
        id = id,
        reservationId = "res-1",
        amount = 450.0,
        status = status,
        method = "SIMULATED",
        gatewayReference = "sim-1",
    )

    @BeforeEach
    fun setUp() {
        paymentService = PaymentService(paymentJpaRepository, reservationJpaRepository, paymentGateway)
    }

    @Test
    @DisplayName("Debería completar el pago y mover la reserva a IN_PROGRESS")
    fun `createPayment should complete payment and move reservation to in progress`() {
        // Given
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { paymentJpaRepository.findByReservationId("res-1") } returns emptyList()
        every { paymentGateway.charge(any()) } returns GatewayChargeResult(approved = true, transactionId = "sim-1")
        every { paymentJpaRepository.save(any()) } returns samplePaymentEntity()
        every { reservationJpaRepository.save(any()) } returns
            sampleReservationEntity(status = ReservationStatus.IN_PROGRESS)

        // When
        val result = paymentService.createPayment("res-1")

        // Then: importe igual al total de la reserva
        assertEquals(PaymentStatus.COMPLETED, result.status)
        assertEquals(450.0, result.amount)
        assertEquals("res-1", result.reservationId)
        assertEquals("sim-1", result.gatewayReference)
        verify { paymentGateway.charge(match { it.amount == 450.0 && !it.simulateFailure }) }
        verify {
            reservationJpaRepository.save(
                match<ReservationEntity> { it.status == ReservationStatus.IN_PROGRESS },
            )
        }
    }

    @Test
    @DisplayName("Debería rechazar pagos para reservas no confirmadas")
    fun `createPayment should reject non-confirmed reservations`() {
        // Given: reserva PENDING
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleReservationEntity(status = ReservationStatus.PENDING))

        // When / Then
        assertThrows<IllegalStateException> { paymentService.createPayment("res-1") }

        // Given: reserva CANCELLED
        every { reservationJpaRepository.findById("res-1") } returns
            Optional.of(sampleReservationEntity(status = ReservationStatus.CANCELLED))

        // When / Then: sin tocar pagos ni gateway
        assertThrows<IllegalStateException> { paymentService.createPayment("res-1") }
        verify(exactly = 0) { paymentJpaRepository.save(any()) }
        verify(exactly = 0) { paymentGateway.charge(any()) }
    }

    @Test
    @DisplayName("Debería devolver 404 para reservas inexistentes e ids en blanco")
    fun `createPayment should throw not found for missing reservations`() {
        // Given
        every { reservationJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertThrows<NoSuchElementException> { paymentService.createPayment("missing") }
        assertThrows<IllegalArgumentException> { paymentService.createPayment("  ") }
        verify(exactly = 0) { paymentJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería rechazar duplicados pero permitir reintento tras FAILED")
    fun `createPayment should reject duplicates but allow retry after failure`() {
        // Given: pago COMPLETED previo
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { paymentJpaRepository.findByReservationId("res-1") } returns listOf(samplePaymentEntity())

        // When / Then
        assertThrows<IllegalStateException> { paymentService.createPayment("res-1") }
        verify(exactly = 0) { paymentJpaRepository.save(any()) }

        // Given: solo un pago FAILED previo
        every { paymentJpaRepository.findByReservationId("res-1") } returns
            listOf(samplePaymentEntity(id = "pay-old", status = PaymentStatus.FAILED))
        every { paymentGateway.charge(any()) } returns GatewayChargeResult(approved = true, transactionId = "sim-2")
        every { paymentJpaRepository.save(any()) } returns samplePaymentEntity().copy(gatewayReference = "sim-2")
        every { reservationJpaRepository.save(any()) } returns
            sampleReservationEntity(status = ReservationStatus.IN_PROGRESS)

        // When
        val retry = paymentService.createPayment("res-1")

        // Then
        assertEquals(PaymentStatus.COMPLETED, retry.status)
        assertEquals("sim-2", retry.gatewayReference)
        verify { paymentJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería persistir FAILED y mantener la reserva CONFIRMED")
    fun `createPayment should persist failed status and keep reservation confirmed`() {
        // Given: el gateway rechaza el cobro
        every { reservationJpaRepository.findById("res-1") } returns Optional.of(sampleReservationEntity())
        every { paymentJpaRepository.findByReservationId("res-1") } returns emptyList()
        every { paymentGateway.charge(any()) } returns GatewayChargeResult(approved = false, transactionId = "sim-9")
        every { paymentJpaRepository.save(any()) } returns samplePaymentEntity(status = PaymentStatus.FAILED)

        // When
        val result = paymentService.createPayment("res-1", simulateFailure = true)

        // Then
        assertEquals(PaymentStatus.FAILED, result.status)
        verify { paymentGateway.charge(match { it.simulateFailure }) }
        verify(exactly = 0) { reservationJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería reembolsar un pago completado")
    fun `refundPayment should refund completed payments`() {
        // Given
        every { paymentJpaRepository.findById("pay-1") } returns Optional.of(samplePaymentEntity())
        every { paymentGateway.refund(any()) } returns GatewayRefundResult(refunded = true, transactionId = "sim-1")
        every { paymentJpaRepository.save(any()) } returns samplePaymentEntity(status = PaymentStatus.REFUNDED)

        // When
        val result = paymentService.refundPayment("pay-1")

        // Then
        assertEquals(PaymentStatus.REFUNDED, result?.status)
        verify { paymentGateway.refund(match { it.transactionId == "sim-1" && it.amount == 450.0 }) }
    }

    @Test
    @DisplayName("Debería rechazar reembolsos de pagos no completados")
    fun `refundPayment should reject non-completed payments`() {
        // Given: pago FAILED
        every { paymentJpaRepository.findById("pay-1") } returns
            Optional.of(samplePaymentEntity(status = PaymentStatus.FAILED))

        // When / Then
        assertThrows<IllegalStateException> { paymentService.refundPayment("pay-1") }

        // Given: pago REFUNDED
        every { paymentJpaRepository.findById("pay-1") } returns
            Optional.of(samplePaymentEntity(status = PaymentStatus.REFUNDED))

        // When / Then: sin tocar el gateway
        assertThrows<IllegalStateException> { paymentService.refundPayment("pay-1") }
        verify(exactly = 0) { paymentGateway.refund(any()) }
        verify(exactly = 0) { paymentJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería retornar null para pagos inexistentes")
    fun `should return null for missing payments`() {
        // Given
        every { paymentJpaRepository.findById("missing") } returns Optional.empty()

        // When / Then
        assertNull(paymentService.getPaymentById("missing"))
        assertNull(paymentService.refundPayment("missing"))
        verify(exactly = 0) { paymentJpaRepository.save(any()) }
    }

    @Test
    @DisplayName("Debería listar pagos paginados con filtros combinados")
    fun `getAllPayments should return paginated payments with combined filters`() {
        // Given
        val criteria = PaymentSearchCriteria(reservationId = "res-1", status = PaymentStatus.COMPLETED)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(samplePaymentEntity()), pageable, 1)

        // When
        every {
            paymentJpaRepository.findAll(any<Specification<PaymentEntity>>(), any<Pageable>())
        } returns page

        val (payments, total) = paymentService.getAllPayments(criteria, 0, 10)

        // Then
        assertEquals(1, payments.size)
        assertEquals(1L, total)
        assertEquals("pay-1", payments.first().id)
        verify { paymentJpaRepository.findAll(any<Specification<PaymentEntity>>(), any<Pageable>()) }
    }
}
