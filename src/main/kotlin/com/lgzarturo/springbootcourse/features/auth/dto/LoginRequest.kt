package com.lgzarturo.springbootcourse.features.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Credenciales para iniciar sesión y obtener un JWT")
data class LoginRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(
        description = "Correo electrónico registrado del usuario",
        format = "email",
        example = "ash@pokemon.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val email: String,
    @field:NotBlank
    @field:Size(max = 72)
    @field:Schema(
        description = "Contraseña del usuario",
        format = "password",
        example = "Pikachu123!",
        maxLength = 72,
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val password: String,
)
