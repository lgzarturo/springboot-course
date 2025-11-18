package com.lgzarturo.springbootcourse.users.domain

import com.lgzarturo.springbootcourse.users.adapters.rest.dto.response.UserResponse
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Email
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Password
import com.lgzarturo.springbootcourse.users.domain.valueobjects.PhoneNumber

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
