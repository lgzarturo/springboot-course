package com.lgzarturo.springbootcourse.features.auth

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Repository
class TokenRevocationAdapter(
    private val revokedTokenJpaRepository: RevokedTokenJpaRepository,
) : TokenRevocationPort {
    private val logger = LoggerFactory.getLogger(TokenRevocationAdapter::class.java)

    override fun isRevoked(tokenId: UUID): Boolean = revokedTokenJpaRepository.existsById(tokenId)

    @Transactional
    override fun revoke(
        tokenId: UUID,
        userId: Long,
        expiresAt: Instant,
        reason: RevocationReason,
    ) {
        if (revokedTokenJpaRepository.existsById(tokenId)) {
            return
        }
        try {
            revokedTokenJpaRepository.save(
                RevokedTokenEntity(
                    jti = tokenId,
                    userId = userId,
                    expiresAt = expiresAt,
                    revokedAt = Instant.now(),
                    reason = reason,
                ),
            )
        } catch (ex: DataIntegrityViolationException) {
            logger.debug("Concurrent revoke of already revoked token", ex)
        }
    }

    @Transactional
    override fun purgeExpired(now: Instant): Int = revokedTokenJpaRepository.deleteByExpiresAtBefore(now)
}
