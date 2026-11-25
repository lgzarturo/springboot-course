package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.config.security.ParsedToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val tokenRevocationPort: TokenRevocationPort,
) {
    fun logout(authentication: Authentication) {
        val parsed = authentication.details as? ParsedToken ?: return
        val userId = authentication.principal as? Long ?: return
        tokenRevocationPort.revoke(parsed.tokenId, userId, parsed.expiresAt, RevocationReason.LOGOUT)
    }
}
