package com.lgzarturo.springbootcourse.features.reservations

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ReservationJpaRepository :
    JpaRepository<ReservationEntity, String>,
    JpaSpecificationExecutor<ReservationEntity> {
    @Query(
        "SELECT COUNT(r) > 0 FROM ReservationEntity r " +
            "WHERE r.room.id = :roomId AND r.status <> 'CANCELLED' " +
            "AND r.checkIn < :checkOut AND r.checkOut > :checkIn",
    )
    fun existsOverlapping(
        @Param("roomId") roomId: String,
        @Param("checkIn") checkIn: LocalDate,
        @Param("checkOut") checkOut: LocalDate,
    ): Boolean
}
