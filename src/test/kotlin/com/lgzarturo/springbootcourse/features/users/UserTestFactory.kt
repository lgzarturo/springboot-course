package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.dto.UserResponse
import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber

fun mockUser(
    email: Email = Email("ash@pokemon.com"),
    password: Password = Password("Pikachu123!"),
    firstName: String = "Ash",
    lastName: String = "Ketchum",
    phoneNumber: PhoneNumber = PhoneNumber("+14155552671"),
    role: UserRole = UserRole.GUEST,
): User =
    User.create(
        email = email,
        password = password,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        role = role,
    )

fun mockUserResponse(): UserResponse =
    UserResponse(
        id = 1L,
        email = "ash@pokemon.com",
        firstName = "Ash",
        lastName = "Ketchum",
        phoneNumber = "+14155552671",
        role = "GUEST",
    )
