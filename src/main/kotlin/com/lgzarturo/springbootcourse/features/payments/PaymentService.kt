package com.lgzarturo.springbootcourse.features.payments

import com.lgzarturo.springbootcourse.features.reservations.ReservationJpaRepository
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentJpaRepository: PaymentJpaRepository,
    private val reservationJpaRepository: ReservationJpaRepository,
    private val paymentGateway: PaymentGateway,
) {
    @Transactional
    fun createPayment(
        reservationId: String,
        method: String = "SIMULATED",
        simulateFailure: Boolean = false,
    ): Payment {
        require(reservationId.isNotBlank()) { "La reserva no puede estar vacía" }
        val reservation =
            reservationJpaRepository.findById(reservationId).orElseThrow {
                NoSuchElementException("Reserva no encontrada: $reservationId")
            }
        check(reservation.status == ReservationStatus.CONFIRMED) {
            "Solo se puede pagar una reserva CONFIRMED (actual: ${reservation.status})"
        }
        val existing = paymentJpaRepository.findByReservationId(reservationId)
        check(existing.none { it.status != PaymentStatus.FAILED }) {
            "La reserva ya tiene un pago activo"
        }
        val pending =
            Payment(
                id = "",
                reservationId = reservationId,
                amount = reservation.totalPrice,
                method = method.ifBlank { "SIMULATED" },
            )
        val result =
            paymentGateway.charge(
                GatewayChargeRequest(
                    amount = pending.amount,
                    reference = reservationId,
                    simulateFailure = simulateFailure,
                ),
            )
        val settled =
            if (result.approved) {
                pending.transitionTo(PaymentStatus.COMPLETED)
            } else {
                pending.transitionTo(PaymentStatus.FAILED)
            }
        val saved = paymentJpaRepository.save(settled.toEntity(result.transactionId))
        if (saved.status == PaymentStatus.COMPLETED) {
            reservationJpaRepository.save(reservation.copy(status = ReservationStatus.IN_PROGRESS))
        }
        return saved.toDomain()
    }

    @Transactional(readOnly = true)
    fun getPaymentById(id: String): Payment? = paymentJpaRepository.findById(id).orElse(null)?.toDomain()

    @Transactional(readOnly = true)
    fun getAllPayments(
        criteria: PaymentSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Payment>, Long> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by("reservationId"))
        val pageResult = paymentJpaRepository.findAll(toSpecification(criteria), pageable)
        return pageResult.content.map { it.toDomain() } to pageResult.totalElements
    }

    @Transactional
    fun refundPayment(id: String): Payment? {
        val existing = paymentJpaRepository.findById(id).orElse(null) ?: return null
        check(existing.status == PaymentStatus.COMPLETED) {
            "Solo se puede reembolsar un pago COMPLETED (actual: ${existing.status})"
        }
        val result =
            paymentGateway.refund(
                GatewayRefundRequest(transactionId = existing.gatewayReference, amount = existing.amount),
            )
        check(result.refunded) { "El gateway rechazó el reembolso" }
        return paymentJpaRepository.save(existing.copy(status = PaymentStatus.REFUNDED)).toDomain()
    }

    private fun toSpecification(criteria: PaymentSearchCriteria): Specification<PaymentEntity> =
        Specification { root, _, builder ->
            val predicates = mutableListOf<Predicate>()
            criteria.reservationId?.let { predicates.add(builder.equal(root.get<String>("reservationId"), it)) }
            criteria.status?.let { predicates.add(builder.equal(root.get<PaymentStatus>("status"), it)) }
            builder.and(*predicates.toTypedArray())
        }

    private fun Payment.toEntity(transactionId: String): PaymentEntity =
        PaymentEntity(
            id = id.ifEmpty { null },
            reservationId = reservationId,
            amount = amount,
            status = status,
            method = method,
            gatewayReference = transactionId,
        )

    private fun PaymentEntity.toDomain(): Payment =
        Payment(
            id = id ?: "",
            reservationId = reservationId,
            amount = amount,
            status = status,
            method = method,
            gatewayReference = gatewayReference,
        )
}
