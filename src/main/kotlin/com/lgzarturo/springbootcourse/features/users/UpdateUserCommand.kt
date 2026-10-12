package com.lgzarturo.springbootcourse.features.users

data class UpdateUserCommand(
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
)
