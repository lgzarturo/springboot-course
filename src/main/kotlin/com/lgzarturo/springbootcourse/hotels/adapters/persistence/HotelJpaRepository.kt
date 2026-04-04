package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface HotelJpaRepository :
    JpaRepository<HotelEntity, String>,
    JpaSpecificationExecutor<HotelEntity> {
    @EntityGraph(attributePaths = ["rooms"])
    override fun findById(id: String): Optional<HotelEntity>

    @Query("SELECT h FROM HotelEntity h LEFT JOIN FETCH h.rooms WHERE h.id = :id")
    fun findByIdWithRooms(
        @Param("id") id: String,
    ): Optional<HotelEntity>

    @Query("SELECT DISTINCT h FROM HotelEntity h LEFT JOIN FETCH h.rooms")
    @EntityGraph(attributePaths = ["rooms"])
    fun findAllWithRooms(
        spec: Specification<HotelEntity>,
        pageable: Pageable,
    ): Page<HotelEntity>
}
