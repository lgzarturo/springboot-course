package com.lgzarturo.springbootcourse.features.reservations

import com.lgzarturo.springbootcourse.features.rooms.RoomEntity
import com.lgzarturo.springbootcourse.features.rooms.RoomJpaRepository
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationService(
    private val reservationJpaRepository: ReservationJpaRepository,
    private val roomJpaRepository: RoomJpaRepository,
) {
    @Transactional
    fun createReservation(reservation: Reservation): Reservation {
        reservation.validateDates()
        val room =
            roomJpaRepository.findById(reservation.roomId).orElseThrow {
                IllegalArgumentException("Habitación no encontrada: ${reservation.roomId}")
            }
        if (reservationJpaRepository.existsOverlapping(reservation.roomId, reservation.checkIn, reservation.checkOut)) {
            throw IllegalStateException("La habitación ya está reservada en esas fechas")
        }
        val total = Reservation.calculateTotalPrice(room.price, reservation.checkIn, reservation.checkOut)
        val saved = reservationJpaRepository.save(reservation.toEntity(room, total))
        return saved.toDomain()
    }

    @Transactional(readOnly = true)
    fun getReservationById(id: String): Reservation? = reservationJpaRepository.findById(id).orElse(null)?.toDomain()

    @Transactional(readOnly = true)
    fun getAllReservations(
        criteria: ReservationSearchCriteria,
        page: Int,
        size: Int,
    ): Pair<List<Reservation>, Long> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by("checkIn"))
        val pageResult = reservationJpaRepository.findAll(toSpecification(criteria), pageable)
        return pageResult.content.map { it.toDomain() } to pageResult.totalElements
    }

    @Transactional
    fun updateStatus(
        id: String,
        target: ReservationStatus,
    ): Reservation? {
        val existing = reservationJpaRepository.findById(id).orElse(null) ?: return null
        val transitioned = existing.toDomain().transitionTo(target)
        syncRoomStatus(existing, target)
        return reservationJpaRepository.save(existing.copy(status = transitioned.status)).toDomain()
    }

    private fun syncRoomStatus(
        existing: ReservationEntity,
        target: ReservationStatus,
    ) {
        val roomId = existing.room?.id ?: return
        when (target) {
            ReservationStatus.CONFIRMED -> updateRoomStatus(roomId, RoomStatus.RESERVED)
            ReservationStatus.CANCELLED -> {
                val room = roomJpaRepository.findById(roomId).orElse(null)
                if (room?.status == RoomStatus.RESERVED) {
                    updateRoomStatus(roomId, RoomStatus.AVAILABLE)
                }
            }
            ReservationStatus.PENDING,
            ReservationStatus.IN_PROGRESS,
            ReservationStatus.COMPLETED,
            -> Unit
        }
    }

    private fun updateRoomStatus(
        roomId: String,
        status: RoomStatus,
    ) {
        val room = roomJpaRepository.findById(roomId).orElse(null) ?: return
        roomJpaRepository.save(room.copy(status = status))
    }

    private fun toSpecification(criteria: ReservationSearchCriteria): Specification<ReservationEntity> =
        Specification { root, _, builder ->
            val predicates = mutableListOf<Predicate>()
            criteria.userId?.let { predicates.add(builder.equal(root.get<Long>("userId"), it)) }
            criteria.status?.let { predicates.add(builder.equal(root.get<ReservationStatus>("status"), it)) }
            criteria.from?.let { predicates.add(builder.greaterThanOrEqualTo(root.get("checkOut"), it)) }
            criteria.to?.let { predicates.add(builder.lessThanOrEqualTo(root.get("checkIn"), it)) }
            builder.and(*predicates.toTypedArray())
        }

    private fun Reservation.toEntity(
        room: RoomEntity,
        total: Double,
    ): ReservationEntity =
        ReservationEntity(
            id = id.ifEmpty { null },
            room = room,
            userId = userId,
            guestName = guestName,
            checkIn = checkIn,
            checkOut = checkOut,
            totalPrice = total,
            status = ReservationStatus.PENDING,
        )

    private fun ReservationEntity.toDomain(): Reservation =
        Reservation(
            id = id ?: "",
            roomId = room?.id ?: "",
            hotelId = room?.hotel?.id ?: "",
            userId = userId,
            guestName = guestName,
            checkIn = checkIn,
            checkOut = checkOut,
            totalPrice = totalPrice,
            status = status,
        )
}
