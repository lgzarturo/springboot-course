package com.lgzarturo.springbootcourse.features.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "revoked_tokens")
data class RevokedTokenEntity(
    @Id
    val jti: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    @Column(name = "revoked_at", nullable = false)
    val revokedAt: Instant,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val reason: RevocationReason,
)
