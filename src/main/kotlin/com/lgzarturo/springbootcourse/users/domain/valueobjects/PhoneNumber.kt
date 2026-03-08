package com.lgzarturo.springbootcourse.users.domain.valueobjects

@JvmInline
value class PhoneNumber(
    val value: String,
) {
    init {
        require(isValid(value)) { "Invalid phone number format: $value" }
    }

    companion object {
        private val PHONE_REGEX = "^\\+?[1-9]\\d{1,14}$".toRegex()

        fun isValid(phone: String): Boolean = PHONE_REGEX.matches(phone)
    }
}
