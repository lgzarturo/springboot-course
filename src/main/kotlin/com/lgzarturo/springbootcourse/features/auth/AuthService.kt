package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.auth.exceptions.InvalidCredentialsException
import com.lgzarturo.springbootcourse.features.users.PasswordEncoder
import com.lgzarturo.springbootcourse.features.users.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    fun authenticate(
        email: String,
        password: String,
    ): String {
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
        if (!user.isActive || !passwordEncoder.matches(password, user.password.value)) {
            throw InvalidCredentialsException()
        }
        return jwtService.generateAccessToken(user.email.value, user.role.allGrantedAuthorities())
    }
}
