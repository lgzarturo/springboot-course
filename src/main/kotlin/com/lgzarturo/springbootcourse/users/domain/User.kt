package com.lgzarturo.springbootcourse.users.domain

import com.lgzarturo.springbootcourse.users.domain.valueobjects.Email
import com.lgzarturo.springbootcourse.users.domain.valueobjects.Password
import com.lgzarturo.springbootcourse.users.domain.valueobjects.PhoneNumber
import com.lgzarturo.springbootcourse.users.domain.valueobjects.UserId

data class User(
    val id: UserId,
    val email: Email,
    val password: Password,
    val firstName: String,
    val lastName: String,
    val phoneNumber: PhoneNumber?,
    val role: UserRole,
    val isActive: Boolean,
) {
    companion object {
        fun create(
            email: Email,
            password: Password,
            firstName: String,
            lastName: String,
            phoneNumber: PhoneNumber?,
            role: UserRole = UserRole.GUEST,
        ): User {
            require(firstName.isNotBlank()) { "First name cannot be blank" }
            require(lastName.isNotBlank()) { "Last name cannot be blank" }

            return User(
                id = UserId.generate(),
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                role = role,
                isActive = true,
            )
        }
    }

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        phoneNumber: PhoneNumber?,
    ): User =
        copy(
            firstName = firstName ?: this.firstName,
            lastName = lastName ?: this.lastName,
            phoneNumber = phoneNumber ?: this.phoneNumber,
        )

    fun changePassword(newPassword: Password): User =
        copy(
            password = newPassword,
        )

    fun deactivate(): User =
        copy(
            isActive = false,
        )

    fun fullName(): String = "$firstName $lastName"

    fun isAdmin(): Boolean = role == UserRole.ADMIN

    fun isStaff(): Boolean = role == UserRole.STAFF

    fun isGuest(): Boolean = role == UserRole.GUEST
}
