package com.lgzarturo.springbootcourse.features.auth

import java.time.Instant
import java.util.UUID

interface TokenRevocationPort {
    fun isRevoked(tokenId: UUID): Boolean

    fun revoke(
        tokenId: UUID,
        userId: Long,
        expiresAt: Instant,
        reason: RevocationReason,
    )

    fun purgeExpired(now: Instant): Int
}

enum class RevocationReason {
    LOGOUT,
}
