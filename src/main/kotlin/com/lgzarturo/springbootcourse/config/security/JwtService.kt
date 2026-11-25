package com.lgzarturo.springbootcourse.config.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    private val properties: JwtProperties,
) {
    private val signingKey: SecretKey by lazy { Keys.hmacShaKeyFor(properties.secret.toByteArray()) }

    fun generateAccessToken(
        userId: Long,
        email: String,
        roles: List<String>,
        tokenVersion: Long,
    ): String = generateToken(userId, email, roles, tokenVersion, properties.accessTokenExpiration)

    fun generateToken(
        userId: Long,
        email: String,
        roles: List<String>,
        tokenVersion: Long,
        timeToLive: Duration,
    ): String {
        val now = Instant.now()
        return Jwts
            .builder()
            .subject(userId.toString())
            .issuer(properties.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(timeToLive)))
            .id(UUID.randomUUID().toString())
            .claim(EMAIL_CLAIM, email)
            .claim(ROLES_CLAIM, roles)
            .claim(VERSION_CLAIM, tokenVersion)
            .signWith(signingKey)
            .compact()
    }

    fun parseToken(token: String): ParsedToken? =
        try {
            parseClaims(token)
        } catch (ex: JwtException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }

    private fun parseClaims(token: String): ParsedToken? {
        val claims =
            Jwts
                .parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer)
                .build()
                .parseSignedClaims(token)
                .payload
        val userId = claims.subject?.toLongOrNull()
        val tokenId = runCatching { UUID.fromString(claims.id) }.getOrNull()
        val version = claims.get(VERSION_CLAIM, Number::class.java)?.toLong()
        val expiration = claims.expiration
        if (userId == null || tokenId == null) {
            return null
        }
        if (version == null || expiration == null) {
            return null
        }
        val roles = claims.get(ROLES_CLAIM, List::class.java) ?: emptyList<Any>()
        return ParsedToken(
            userId = userId,
            email = claims.get(EMAIL_CLAIM, String::class.java),
            authorities = roles.map { it.toString() },
            tokenId = tokenId,
            tokenVersion = version,
            expiresAt = expiration.toInstant(),
        )
    }

    companion object {
        const val ROLES_CLAIM = "roles"
        const val EMAIL_CLAIM = "email"
        const val VERSION_CLAIM = "v"
    }
}
