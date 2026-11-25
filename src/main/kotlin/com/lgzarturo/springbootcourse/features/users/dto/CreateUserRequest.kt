package com.lgzarturo.springbootcourse.features.users.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Datos para registrar un usuario con rol GUEST")
data class CreateUserRequest(
    @field:Email
    @field:NotBlank
    @field:Schema(
        description = "Correo electrónico único del usuario",
        format = "email",
        example = "misty@pokemon.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val email: String,
    @field:NotBlank
    @field:Size(min = 8, max = 72)
    @field:Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$")
    @field:Schema(
        description = "Contraseña de 8 a 72 caracteres con mayúscula, minúscula y dígito",
        format = "password",
        pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$",
        minLength = 8,
        maxLength = 72,
        example = "Starmie123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val password: String,
    @field:NotBlank
    @field:Schema(
        description = "Nombre del usuario",
        example = "Misty",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val firstName: String,
    @field:NotBlank
    @field:Schema(
        description = "Apellido del usuario",
        example = "Waterflower",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val lastName: String,
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @field:Schema(
        description = "Teléfono en formato E.164, opcional",
        pattern = "^\\+?[1-9]\\d{1,14}$",
        example = "+14155552671",
    )
    val phoneNumber: String? = null,
)
