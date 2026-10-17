package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.exceptions.DuplicateEmailException
import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateUserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun execute(command: CreateUserCommand): User {
        if (userRepository.existsByEmail(command.email)) {
            throw DuplicateEmailException(command.email)
        }

        val user =
            User.create(
                email = Email(command.email),
                password = Password.fromEncrypted(passwordEncoder.encode(command.password)),
                firstName = command.firstName,
                lastName = command.lastName,
                phoneNumber = command.phoneNumber?.let(::PhoneNumber),
                role = command.role,
            )

        return userRepository.save(user)
    }
}
