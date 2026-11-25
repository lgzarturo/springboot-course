package com.lgzarturo.springbootcourse.features.auth

import com.lgzarturo.springbootcourse.common.exception.ErrorResponse
import com.lgzarturo.springbootcourse.features.auth.dto.LoginRequest
import com.lgzarturo.springbootcourse.features.auth.dto.LoginResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "Endpoints de autenticación con JWT")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val logoutService: LogoutService,
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

    @Operation(
        summary = "Cerrar sesión",
        description = "Revoca el JWT actual. El token deja de aceptarse de inmediato. Idempotente.",
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Sesión cerrada, token revocado"),
        ApiResponse(
            responseCode = "401",
            description = "Sin autenticación o token inválido",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
        ),
    )
    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<Void> {
        logoutService.logout(authentication)
        return ResponseEntity.noContent().build()
    }
}
