package com.lgzarturo.springbootcourse.features.auth

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RevokedTokenCleanupJob(
    private val tokenRevocationPort: TokenRevocationPort,
) {
    private val logger = LoggerFactory.getLogger(RevokedTokenCleanupJob::class.java)

    @Scheduled(cron = "0 0 3 * * *")
    fun purgeExpiredTokens() {
        val removed = tokenRevocationPort.purgeExpired(Instant.now())
        logger.info("Purged {} expired revoked tokens", removed)
    }
}
