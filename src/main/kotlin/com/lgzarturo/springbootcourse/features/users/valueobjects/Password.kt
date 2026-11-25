package com.lgzarturo.springbootcourse.features.users.valueobjects

@JvmInline
value class Password(
    val value: String,
) {
    init {
        require(isSecure(value) || isEncrypted(value)) {
            "Password must be 8 to 72 characters and contain uppercase, lowercase, and number"
        }
    }

    companion object {
        private val PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$".toRegex()

        fun isSecure(password: String): Boolean = PASSWORD_REGEX.matches(password)

        fun fromEncrypted(encrypted: String): Password = Password(encrypted)

        private fun isEncrypted(password: String): Boolean = password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$".toRegex())
    }
}
