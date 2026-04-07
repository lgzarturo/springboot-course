package com.lgzarturo.springbootcourse.features.users.dto

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
)
