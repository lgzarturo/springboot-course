package com.lgzarturo.springbootcourse.users.adapters.rest.dto.response

import com.lgzarturo.springbootcourse.users.domain.UserRole

data class UserResponse(
    val id: Long? = null,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val role: String,
)
