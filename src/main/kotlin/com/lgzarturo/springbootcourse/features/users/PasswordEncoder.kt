package com.lgzarturo.springbootcourse.features.users

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    private val delegate = BCryptPasswordEncoder()

    fun encode(rawPassword: CharSequence): String = requireNotNull(delegate.encode(rawPassword))

    fun matches(
        rawPassword: CharSequence,
        encodedPassword: String,
    ): Boolean = delegate.matches(rawPassword, encodedPassword)
}
