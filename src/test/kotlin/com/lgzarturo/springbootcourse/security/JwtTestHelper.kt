package com.lgzarturo.springbootcourse.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date

/**
 * Mints and parses JWTs with raw JJWT for security tests.
 * Independent from the production JwtService: tokens are built directly
 * with the configured test secret so tests verify the real filter behavior.
 */
object JwtTestHelper {
    fun mintToken(
        secret: String,
        subject: String,
        roles: List<String>,
        issuer: String,
        ttlMillis: Long,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(subject)
            .issuer(issuer)
            .issuedAt(now)
            .expiration(Date(now.time + ttlMillis))
            .claim("roles", roles)
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()
    }

    fun parseClaims(
        token: String,
        secret: String,
    ): Claims =
        Jwts
            .parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload
}
