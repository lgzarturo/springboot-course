package com.lgzarturo.springbootcourse.support

import com.lgzarturo.springbootcourse.features.users.User
import com.lgzarturo.springbootcourse.security.JwtTestHelper

/**
 * Shared helpers and fixtures for layered feature testing (BC-009).
 */
object LayeredTestSupport {
    fun uniqueEmail(prefix: String = "trainer"): String = "$prefix.${System.nanoTime()}@pokemon.com"

    fun uniquePhone(): String = "+1415${(1000000..9999999).random()}"

    fun mintTokenForUser(
        secret: String,
        user: User,
        issuer: String = "springboot-course-test",
    ): String = JwtTestHelper.mintTokenForUser(secret, user, issuer)
}
