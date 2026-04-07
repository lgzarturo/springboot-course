package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.features.users.valueobjects.UserId

class CreateUserService(
    @Suppress("UnusedPrivateProperty")
    private val userRepository: UserRepository,
    @Suppress("UnusedPrivateProperty")
    private val passwordEncoder: PasswordEncoder,
) {
    fun execute(command: CreateUserCommand): User {
        // PENDING: Implement this method
        println("Executing CreateUserService with command: $command")
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
