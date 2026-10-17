package com.lgzarturo.springbootcourse.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpiration: Duration,
    val issuer: String,
) {
    init {
        require(secret.toByteArray().size >= MIN_SECRET_BYTES) { "jwt.secret must be at least 256 bits" }
    }

    companion object {
        const val MIN_SECRET_BYTES = 32
    }
}
