package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import org.springframework.stereotype.Component

@Component
class UserMapper {
    fun toResponse(user: User): UserResponse =
        UserResponse(
            id = user.id.value,
            email = user.email.value,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber?.value,
            role = user.role.name,
        )
}
