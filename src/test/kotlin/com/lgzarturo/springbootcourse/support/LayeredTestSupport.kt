package com.lgzarturo.springbootcourse.support

import com.lgzarturo.springbootcourse.features.users.UserRole
import com.lgzarturo.springbootcourse.security.JwtTestHelper

/**
 * Shared helpers and fixtures for layered feature testing (BC-009).
 */
object LayeredTestSupport {
    fun uniqueEmail(prefix: String = "trainer"): String =
        "$prefix.${System.nanoTime()}@pokemon.com"

    fun uniquePhone(): String =
        "+1415${(1000000..9999999).random()}"

    fun mintTokenForRole(
        secret: String,
        email: String,
        role: UserRole,
        issuer: String = "springboot-course-test",
    ): String =
        JwtTestHelper.mintToken(
            secret = secret,
            subject = email,
            roles = role.allGrantedAuthorities(),
            issuer = issuer,
            ttlMillis = 3600_000,
        )
}
