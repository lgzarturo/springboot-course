package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.config.security.JwtProperties
import com.lgzarturo.springbootcourse.config.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

@DisplayName("JwtService Tests")
class JwtServiceTest {
    private val properties =
        JwtProperties(
            secret = "UnitTestSecretKeyForJwtSigningThatIsLongEnough123456",
            accessTokenExpiration = Duration.ofMinutes(15),
            issuer = "test-issuer",
        )
    private val jwtService = JwtService(properties)

    @Test
    @DisplayName("Debe generar un token válido con sub=userId y claims email, jti y v")
    fun `generate then parse returns verified claims`() {
        val token = jwtService.generateAccessToken(42L, "ash@pokemon.com", listOf("ROLE_GUEST"), 3L)

        val parsed = jwtService.parseToken(token)

        assertNotNull(parsed)
        assertEquals(42L, parsed?.userId)
        assertEquals("ash@pokemon.com", parsed?.email)
        assertEquals(listOf("ROLE_GUEST"), parsed?.authorities)
        assertNotNull(parsed?.tokenId)
        assertEquals(3L, parsed?.tokenVersion)
        assertTrue(parsed?.expiresAt?.isAfter(Instant.now()) == true)
    }

    @Test
    @DisplayName("Cada emisión debe producir un jti distinto")
    fun `each emission mints a unique jti`() {
        val first = jwtService.parseToken(jwtService.generateAccessToken(1L, "ash@pokemon.com", listOf("ROLE_GUEST"), 0L))
        val second = jwtService.parseToken(jwtService.generateAccessToken(1L, "ash@pokemon.com", listOf("ROLE_GUEST"), 0L))

        assertNotNull(first?.tokenId)
        assertNotEquals(first?.tokenId, second?.tokenId)
    }

    @Test
    @DisplayName("Un token sin claim de roles debe parsear con autoridades vacías (M4)")
    fun `token without roles claim parses with empty authorities`() {
        val token =
            JwtTestHelper.mintTokenV2(
                secret = properties.secret,
                subject = "7",
                email = "ash@pokemon.com",
                roles = null,
                issuer = properties.issuer,
                ttlMillis = 60_000,
                version = 0,
            )

        val parsed = jwtService.parseToken(token)

        assertNotNull(parsed)
        assertEquals(emptyList<String>(), parsed?.authorities)
    }

    @Test
    @DisplayName("Debe rechazar tokens legacy sin jti ni versión")
    fun `legacy token without jti and version returns null`() {
        val legacy =
            JwtTestHelper.mintToken(properties.secret, "ash@pokemon.com", listOf("ROLE_GUEST"), properties.issuer, 60_000)

        assertNull(jwtService.parseToken(legacy))
    }

    @Test
    @DisplayName("Debe rechazar un token expirado")
    fun `expired token returns null`() {
        val token = jwtService.generateToken(1L, "ash@pokemon.com", listOf("ROLE_GUEST"), 0L, Duration.ofMillis(-1000))

        assertNull(jwtService.parseToken(token))
    }

    @Test
    @DisplayName("Debe rechazar un token firmado con otro secreto")
    fun `wrong signature returns null`() {
        val otherService =
            JwtService(properties.copy(secret = "AnotherUnitTestSecretKeyForJwtSigningLongEnough12"))
        val token = otherService.generateAccessToken(1L, "ash@pokemon.com", listOf("ROLE_GUEST"), 0L)

        assertNull(jwtService.parseToken(token))
    }

    @Test
    @DisplayName("Debe rechazar un token de otro emisor")
    fun `wrong issuer returns null`() {
        val otherService = JwtService(properties.copy(issuer = "other-issuer"))
        val token = otherService.generateAccessToken(1L, "ash@pokemon.com", listOf("ROLE_GUEST"), 0L)

        assertNull(jwtService.parseToken(token))
    }

    @Test
    @DisplayName("Debe rechazar un token malformado")
    fun `malformed token returns null`() {
        assertNull(jwtService.parseToken("not-a-jwt"))
        assertNull(jwtService.parseToken(""))
    }
}
