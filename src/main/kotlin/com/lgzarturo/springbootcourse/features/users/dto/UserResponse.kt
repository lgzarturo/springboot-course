package com.lgzarturo.springbootcourse.features.users.dto

data class UserResponse(
    val id: Long? = null,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val role: String,
)
