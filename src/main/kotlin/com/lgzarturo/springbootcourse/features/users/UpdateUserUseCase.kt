package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateUserUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        id: Long,
        command: UpdateUserCommand,
    ): User {
        val user = userRepository.findById(id) ?: throw NoSuchElementException("User $id not found")
        val updated =
            user.updateProfile(
                firstName = command.firstName,
                lastName = command.lastName,
                phoneNumber = command.phoneNumber?.let(::PhoneNumber),
            )
        return userRepository.save(updated)
    }

    @Transactional
    fun deactivate(id: Long): User {
        val user = userRepository.findById(id) ?: throw NoSuchElementException("User $id not found")
        return userRepository.save(user.deactivate())
    }
}
