package com.lgzarturo.springbootcourse.features.reviews

import com.lgzarturo.springbootcourse.features.reservations.ReservationEntity
import com.lgzarturo.springbootcourse.features.reservations.ReservationJpaRepository
import com.lgzarturo.springbootcourse.features.reservations.ReservationStatus
import com.lgzarturo.springbootcourse.features.users.UserJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewJpaRepository: ReviewJpaRepository,
    private val reservationJpaRepository: ReservationJpaRepository,
    private val userJpaRepository: UserJpaRepository,
) {
    @Transactional
    fun createReview(
        reservationId: String,
        rating: Int,
        comment: String,
        authenticatedEmail: String,
    ): Review {
        require(reservationId.isNotBlank()) { "La reserva no puede estar vacía" }
        val reservation =
            reservationJpaRepository.findById(reservationId).orElseThrow {
                NoSuchElementException("Reserva no encontrada: $reservationId")
            }
        check(reservation.status == ReservationStatus.COMPLETED) {
            "Solo se puede opinar sobre una reserva COMPLETED (actual: ${reservation.status})"
        }
        checkOwnership(reservation.userId, authenticatedEmail)
        check(!reviewJpaRepository.existsByReservationId(reservationId)) {
            "La reserva ya tiene una reseña"
        }
        val hotelId = resolveHotelId(reservation)
        val review =
            Review(
                id = "",
                reservationId = reservationId,
                hotelId = hotelId,
                userId = reservation.userId,
                rating = rating,
                comment = comment,
            )
        return reviewJpaRepository.save(review.toEntity()).toDomain()
    }

    @Transactional(readOnly = true)
    fun getReviewById(id: String): Review? = reviewJpaRepository.findById(id).orElse(null)?.toDomain()

    @Transactional(readOnly = true)
    fun getHotelReviews(
        hotelId: String,
        page: Int,
        size: Int,
    ): Triple<List<Review>, Long, Double> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by("id"))
        val pageResult = reviewJpaRepository.findByHotelId(hotelId, pageable)
        val average = reviewJpaRepository.averageRatingByHotelId(hotelId)
        return Triple(pageResult.content.map { it.toDomain() }, pageResult.totalElements, average)
    }

    private fun checkOwnership(
        ownerId: Long,
        authenticatedEmail: String,
    ) {
        val user =
            userJpaRepository.findByEmail(authenticatedEmail)
                ?: throw NoSuchElementException("Usuario no encontrado: $authenticatedEmail")
        if (ownerId != user.id) {
            throw AccessDeniedException("Solo el propietario de la reserva puede crear su reseña")
        }
    }

    private fun resolveHotelId(reservation: ReservationEntity): String =
        reservation.room?.hotel?.id
            ?: throw IllegalStateException("La reserva no tiene hotel asociado")

    private fun Review.toEntity(): ReviewEntity =
        ReviewEntity(
            id = id.ifEmpty { null },
            reservationId = reservationId,
            hotelId = hotelId,
            userId = userId,
            rating = rating,
            comment = comment,
        )

    private fun ReviewEntity.toDomain(): Review =
        Review(
            id = id ?: "",
            reservationId = reservationId,
            hotelId = hotelId,
            userId = userId,
            rating = rating,
            comment = comment,
        )
}
