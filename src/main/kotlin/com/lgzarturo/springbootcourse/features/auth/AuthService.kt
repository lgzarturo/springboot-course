package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.auth.exceptions.InvalidCredentialsException
import com.lgzarturo.springbootcourse.features.users.PasswordEncoder
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserDomainRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun authenticate(
        email: String,
        password: String,
    ): String {
        val user = userRepository.findByEmail(email.lowercase())
        val passwordOk = passwordEncoder.matches(password, user?.password?.value ?: DUMMY_HASH)
        if (user == null || !user.isActive || !passwordOk) {
            throw InvalidCredentialsException()
        }
        return jwtService.generateAccessToken(
            user.id.value,
            user.email.value,
            user.role.allGrantedAuthorities(),
            user.tokenVersion,
        )
    }

    companion object {
        // Hash bcrypt válido para comparar cuando el usuario no existe:
        // iguala los tiempos y evita enumeración por timing.
        private const val DUMMY_HASH = "\$2a\$12\$qBcsQSN9W3MTd9rhlZT1QegAgwhVGri9msaYm8dTO8A7qLz9vWWVG"
    }
}
