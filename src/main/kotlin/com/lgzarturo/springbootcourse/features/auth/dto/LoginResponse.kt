package com.lgzarturo.springbootcourse.features.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Token JWT emitido tras un inicio de sesión exitoso")
data class LoginResponse(
    @field:Schema(
        description = "JWT firmado para usar como `Bearer <token>`",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhc2hAcG9rZW1vbi5jb20ifQ.firma",
    )
    val token: String,
)
