package com.lgzarturo.springbootcourse.config.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth.rate-limit")
data class RateLimitProperties(
    val enabled: Boolean = true,
    val perMinute: Int = 5,
    val windowSeconds: Long = 60,
)
