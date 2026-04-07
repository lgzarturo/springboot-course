package com.lgzarturo.springbootcourse.features.users.exceptions

class DuplicateEmailException(
    email: String,
) : RuntimeException("Email already exists: $email")
