package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.config.security.JwtProperties
import com.lgzarturo.springbootcourse.config.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Duration

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
    @DisplayName("Debe generar un token válido con subject y autoridades")
    fun `generate then parse returns authentication`() {
        val token = jwtService.generateAccessToken("ash@pokemon.com", listOf("ROLE_GUEST"))

        val authentication = jwtService.parseAuthentication(token)

        assertNotNull(authentication)
        assertEquals("ash@pokemon.com", authentication?.name)
        assertTrue(authentication?.authorities?.any { it.authority == "ROLE_GUEST" } == true)
    }

    @Test
    @DisplayName("Debe rechazar un token expirado")
    fun `expired token returns null`() {
        val token = jwtService.generateToken("ash@pokemon.com", listOf("ROLE_GUEST"), Duration.ofMillis(-1000))

        assertNull(jwtService.parseAuthentication(token))
    }

    @Test
    @DisplayName("Debe rechazar un token firmado con otro secreto")
    fun `wrong signature returns null`() {
        val otherService =
            JwtService(properties.copy(secret = "AnotherUnitTestSecretKeyForJwtSigningLongEnough12"))
        val token = otherService.generateAccessToken("ash@pokemon.com", listOf("ROLE_GUEST"))

        assertNull(jwtService.parseAuthentication(token))
    }

    @Test
    @DisplayName("Debe rechazar un token de otro emisor")
    fun `wrong issuer returns null`() {
        val otherService = JwtService(properties.copy(issuer = "other-issuer"))
        val token = otherService.generateAccessToken("ash@pokemon.com", listOf("ROLE_GUEST"))

        assertNull(jwtService.parseAuthentication(token))
    }

    @Test
    @DisplayName("Debe rechazar un token malformado")
    fun `malformed token returns null`() {
        assertNull(jwtService.parseAuthentication("not-a-jwt"))
        assertNull(jwtService.parseAuthentication(""))
    }
}
