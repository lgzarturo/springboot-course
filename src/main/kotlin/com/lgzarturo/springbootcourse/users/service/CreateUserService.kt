package com.lgzarturo.springbootcourse.users.service

import com.lgzarturo.springbootcourse.users.adapters.persistence.UserRepository
import com.lgzarturo.springbootcourse.users.domain.CreateUserCommand
import com.lgzarturo.springbootcourse.users.domain.User
import com.lgzarturo.springbootcourse.users.domain.UserRole
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Email
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Password
import com.lgzarturo.springbootcourse.users.domain.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.users.domain.valueobjects.UserId

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
