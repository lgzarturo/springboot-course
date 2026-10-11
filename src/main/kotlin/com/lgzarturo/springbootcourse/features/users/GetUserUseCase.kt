package com.lgzarturo.springbootcourse.features.users

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun execute(id: Long): User = userRepository.findById(id) ?: throw NoSuchElementException("User $id not found")

    @Transactional(readOnly = true)
    fun executeAll(): List<User> = userRepository.findAll()
}
