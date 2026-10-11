package com.lgzarturo.springbootcourse.features.users.exceptions

class DuplicateEmailException(
    email: String,
) : IllegalStateException("Email already exists: $email")
