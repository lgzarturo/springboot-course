package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.auth.exceptions.InvalidCredentialsException
import com.lgzarturo.springbootcourse.features.users.PasswordEncoder
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserDomainRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun authenticate(
        email: String,
        password: String,
    ): String {
        val user = userRepository.findByEmail(email.lowercase())
        val passwordOk = passwordEncoder.matches(password, user?.password?.value ?: DUMMY_HASH)
        if (user == null || !user.isActive || !passwordOk) {
            logger.warn("Authentication failed for email={}", maskEmail(email))
            throw InvalidCredentialsException()
        }
        logger.info("Authentication successful for userId={}", user.id.value)
        return jwtService.generateAccessToken(
            user.id.value,
            user.email.value,
            user.role.allGrantedAuthorities(),
            user.tokenVersion,
        )
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2 || parts[0].isEmpty()) {
            return "***"
        }
        return "${parts[0].first()}***@${parts[1]}"
    }

    companion object {
        // Hash bcrypt válido para comparar cuando el usuario no existe:
        // iguala los tiempos y evita enumeración por timing.
        private const val DUMMY_HASH = "\$2a\$12\$qBcsQSN9W3MTd9rhlZT1QegAgwhVGri9msaYm8dTO8A7qLz9vWWVG"
    }
}
