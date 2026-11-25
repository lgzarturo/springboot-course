package com.lgzarturo.springbootcourse.features.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("RevokedTokenCleanupJob Tests")
class RevokedTokenCleanupJobTest {
    @Test
    @DisplayName("Debe purgar las revocaciones expiradas al ejecutarse")
    fun `purgeExpiredTokens delegates to port`() {
        val port = mockk<TokenRevocationPort>()
        every { port.purgeExpired(any()) } returns 2

        RevokedTokenCleanupJob(port).purgeExpiredTokens()

        verify { port.purgeExpired(any()) }
    }
}
