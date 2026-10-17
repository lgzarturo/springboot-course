package com.lgzarturo.springbootcourse.config.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    private val properties: JwtProperties,
) {
    private val signingKey: SecretKey by lazy { Keys.hmacShaKeyFor(properties.secret.toByteArray()) }

    fun generateAccessToken(
        email: String,
        roles: List<String>,
    ): String = generateToken(email, roles, properties.accessTokenExpiration)

    fun generateToken(
        email: String,
        roles: List<String>,
        timeToLive: Duration,
    ): String {
        val now = Date()
        return Jwts
            .builder()
            .subject(email)
            .issuer(properties.issuer)
            .issuedAt(now)
            .expiration(Date(now.time + timeToLive.toMillis()))
            .claim(ROLES_CLAIM, roles)
            .signWith(signingKey)
            .compact()
    }

    fun parseAuthentication(token: String): Authentication? =
        try {
            val claims =
                Jwts
                    .parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer)
                    .build()
                    .parseSignedClaims(token)
                    .payload
            val authorities =
                claims.get(ROLES_CLAIM, List::class.java).map { SimpleGrantedAuthority(it.toString()) }
            UsernamePasswordAuthenticationToken(claims.subject, null, authorities)
        } catch (ex: JwtException) {
            null
        } catch (ex: IllegalArgumentException) {
            null
        }

    companion object {
        const val ROLES_CLAIM = "roles"
    }
}
