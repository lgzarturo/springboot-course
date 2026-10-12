package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.features.auth.dto.LoginRequest
import com.lgzarturo.springbootcourse.features.auth.dto.LoginResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val token = authService.authenticate(request.email, request.password)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
