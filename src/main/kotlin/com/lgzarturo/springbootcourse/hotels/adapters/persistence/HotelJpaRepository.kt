package com.lgzarturo.springbootcourse.hotels.adapters.persistence

import com.lgzarturo.springbootcourse.hotels.adapters.persistence.entity.HotelEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface HotelJpaRepository :
    JpaRepository<HotelEntity, String>,
    JpaSpecificationExecutor<HotelEntity>
