package com.lgzarturo.springbootcourse.users.adapters.persistence

import com.lgzarturo.springbootcourse.users.domain.User

interface UserRepository {
    fun existsByEmail(email: String): Boolean

    fun save(user: User): User
}
