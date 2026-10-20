package com.lgzarturo.springbootcourse.features.reviews

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReviewJpaRepository : JpaRepository<ReviewEntity, String> {
    fun existsByReservationId(reservationId: String): Boolean

    fun findByHotelId(
        hotelId: String,
        pageable: Pageable,
    ): Page<ReviewEntity>

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ReviewEntity r WHERE r.hotelId = :hotelId")
    fun averageRatingByHotelId(
        @Param("hotelId") hotelId: String,
    ): Double
}
