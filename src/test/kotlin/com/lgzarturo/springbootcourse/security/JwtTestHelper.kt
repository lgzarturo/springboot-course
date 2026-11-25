package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.features.users.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID

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

    fun mintTokenWithoutRoles(
        secret: String,
        subject: String,
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
            .signWith(Keys.hmacShaKeyFor(secret.toByteArray()))
            .compact()
    }

    fun mintTokenV2(
        secret: String,
        subject: String,
        email: String? = null,
        roles: List<String>? = null,
        issuer: String = "springboot-course-test",
        ttlMillis: Long = 3600_000,
        version: Long = 0,
    ): String {
        val now = Date()
        val builder =
            Jwts
                .builder()
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(Date(now.time + ttlMillis))
                .id(UUID.randomUUID().toString())
                .claim("v", version)
        if (email != null) {
            builder.claim("email", email)
        }
        if (roles != null) {
            builder.claim("roles", roles)
        }
        return builder.signWith(Keys.hmacShaKeyFor(secret.toByteArray())).compact()
    }

    fun mintTokenForUser(
        secret: String,
        user: User,
        issuer: String = "springboot-course-test",
        ttlMillis: Long = 3600_000,
    ): String =
        mintTokenV2(
            secret = secret,
            subject = user.id.value.toString(),
            email = user.email.value,
            roles = user.role.allGrantedAuthorities(),
            issuer = issuer,
            ttlMillis = ttlMillis,
            version = user.tokenVersion,
        )

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
