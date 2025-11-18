package com.lgzarturo.springbootcourse.users.domain.valueobjects

@JvmInline
value class Password(
    val value: String,
) {
    init {
        require(isSecure(value)) {
            "Password must be at least 8 characters and contain uppercase, lowercase, and number"
        }
    }

    companion object {
        private val PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$".toRegex()

        fun isSecure(password: String): Boolean = PASSWORD_REGEX.matches(password)

        fun fromEncrypted(encrypted: String): Password = Password(encrypted)
    }
}
