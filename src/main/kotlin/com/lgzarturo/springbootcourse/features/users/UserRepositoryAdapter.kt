package com.lgzarturo.springbootcourse.features.users

import com.lgzarturo.springbootcourse.features.users.valueobjects.Email
import com.lgzarturo.springbootcourse.features.users.valueobjects.Password
import com.lgzarturo.springbootcourse.features.users.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.features.users.valueobjects.UserId
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun existsByEmail(email: String): Boolean = userJpaRepository.existsByEmail(email)

    override fun findByEmail(email: String): User? = userJpaRepository.findByEmail(email)?.toDomain()

    override fun save(user: User): User = userJpaRepository.save(user.toEntity()).toDomain()

    override fun findById(id: Long): User? = userJpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(): List<User> = userJpaRepository.findAll().map { it.toDomain() }

    private fun User.toEntity(): UserEntity =
        UserEntity(
            id = id.value.takeIf { it > 0 },
            email = email.value,
            passwordHash = password.value,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber?.value,
            role = role,
            isActive = isActive,
        )

    private fun UserEntity.toDomain(): User =
        User(
            id = UserId(requireNotNull(id)),
            email = Email(email),
            password = Password.fromEncrypted(passwordHash),
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber?.let(::PhoneNumber),
            role = role,
            isActive = isActive,
        )
}
