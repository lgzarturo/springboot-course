package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.UserResponse

class UserMapper {
    fun toResponse(user: User): UserResponse {
        // PENDING: Implement this method
        println("Converting User to UserResponse")
        println("User: $user")
        return UserResponse(
            id = user.id.value,
            email = user.email.value,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber?.value,
            role = user.role.name,
        )
    }
}
