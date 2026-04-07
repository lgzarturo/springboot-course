package com.lgzarturo.springbootcourse.features.users

interface UserRepository {
    fun existsByEmail(email: String): Boolean

    fun save(user: User): User
}
