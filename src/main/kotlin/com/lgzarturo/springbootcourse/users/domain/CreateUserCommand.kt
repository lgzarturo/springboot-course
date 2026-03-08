package com.lgzarturo.springbootcourse.users.domain

class CreateUserCommand(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val role: UserRole,
) {
    // PENDING: Implement this class
}
