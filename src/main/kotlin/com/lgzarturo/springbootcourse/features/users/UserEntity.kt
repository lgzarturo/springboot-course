package com.lgzarturo.springbootcourse.features.users

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val email: String,
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    @Column(name = "first_name", nullable = false)
    val firstName: String,
    @Column(name = "last_name", nullable = false)
    val lastName: String,
    @Column(name = "phone_number")
    val phoneNumber: String? = null,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole,
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean,
)
