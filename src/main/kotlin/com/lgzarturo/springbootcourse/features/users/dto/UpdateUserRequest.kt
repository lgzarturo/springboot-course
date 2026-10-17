package com.lgzarturo.springbootcourse.features.users.dto

import jakarta.validation.constraints.Pattern

data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    val phoneNumber: String? = null,
)
