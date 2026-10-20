package com.lgzarturo.springbootcourse.features.users.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de un usuario; nunca expone la contraseña")
data class UserResponse(
    @field:Schema(description = "Identificador del usuario", example = "1")
    val id: Long? = null,
    @field:Schema(description = "Correo electrónico del usuario", format = "email", example = "ash@pokemon.com")
    val email: String,
    @field:Schema(description = "Nombre del usuario", example = "Ash")
    val firstName: String?,
    @field:Schema(description = "Apellido del usuario", example = "Ketchum")
    val lastName: String?,
    @field:Schema(description = "Teléfono en formato E.164, si fue registrado", example = "+14155552671")
    val phoneNumber: String?,
    @field:Schema(description = "Rol del usuario", allowableValues = ["GUEST", "STAFF", "ADMIN"], example = "GUEST")
    val role: String,
)
