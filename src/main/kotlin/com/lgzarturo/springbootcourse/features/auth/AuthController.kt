package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.auth.dto.LoginRequest
import com.lgzarturo.springbootcourse.features.auth.dto.LoginResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "Endpoints de autenticación con JWT")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica con correo y contraseña y retorna un JWT firmado. Endpoint público.",
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa con token JWT",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = LoginResponse::class))],
        ),
        ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
        ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val token = authService.authenticate(request.email, request.password)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
