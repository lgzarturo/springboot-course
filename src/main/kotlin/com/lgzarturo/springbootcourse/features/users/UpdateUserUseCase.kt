package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.features.users.valueobjects.UserId

class UpdateUserUseCase {
    fun execute(id: Long): User {
        // PENDING: Implement this method
        println("Executing UpdateUserUseCase with id: $id")
        return User(
            id = UserId.generate(),
            email = Email("ash@pokemon.com"),
            password = Password("Pikachu123!"),
            firstName = "Ash",
            lastName = "Ketchum",
            phoneNumber = PhoneNumber("+14155552671"),
            role = UserRole.GUEST,
            isActive = true,
        )
    }
}
