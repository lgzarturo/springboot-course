package com.lgzarturo.springbootcourse.users.domain.exceptions

class DuplicateEmailException(
    email: String,
) : RuntimeException("Email already exists: $email")
