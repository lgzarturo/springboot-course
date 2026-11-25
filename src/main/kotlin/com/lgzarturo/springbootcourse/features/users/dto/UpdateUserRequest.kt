package com.lgzarturo.springbootcourse.features.users.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern

@Schema(description = "Datos editables del perfil de usuario")
data class UpdateUserRequest(
    @field:Schema(description = "Nombre del usuario", example = "Blue")
    val firstName: String? = null,
    @field:Schema(description = "Apellido del usuario", example = "Oak")
    val lastName: String? = null,
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    @field:Schema(
        description = "Teléfono en formato E.164, opcional",
        pattern = "^\\+?[1-9]\\d{1,14}$",
        example = "+14155559999",
    )
    val phoneNumber: String? = null,
)
