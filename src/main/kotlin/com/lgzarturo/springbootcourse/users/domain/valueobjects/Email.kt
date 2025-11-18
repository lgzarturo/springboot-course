package com.lgzarturo.springbootcourse.users.domain.valueobjects

@JvmInline
value class Email(
    val value: String,
) {
    init {
        require(isValid(value)) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        fun isValid(email: String): Boolean = EMAIL_REGEX.matches(email)
    }
}
