package com.lgzarturo.springbootcourse.users.application.ports.input

import com.lgzarturo.springbootcourse.users.domain.User
import com.lgzarturo.springbootcourse.users.domain.UserRole
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Email
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Password
import com.lgzarturo.springbootcourse.users.domain.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.users.domain.valueobjects.UserId

class GetUserUseCase {
    fun execute(id: Long): User {
        // PENDING: Implement this method
        println("Executing GetUserUseCase with id: $id")
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
