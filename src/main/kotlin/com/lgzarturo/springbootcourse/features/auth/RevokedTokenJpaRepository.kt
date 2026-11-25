package com.lgzarturo.springbootcourse.features.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface RevokedTokenJpaRepository : JpaRepository<RevokedTokenEntity, UUID> {
    fun deleteByExpiresAtBefore(now: Instant): Int
}
