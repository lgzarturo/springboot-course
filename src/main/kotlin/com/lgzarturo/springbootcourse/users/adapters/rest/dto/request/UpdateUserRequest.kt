package com.lgzarturo.springbootcourse.users.adapters.rest.dto.request

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
)
