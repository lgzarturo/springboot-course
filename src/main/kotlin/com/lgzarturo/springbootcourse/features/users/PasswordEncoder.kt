package com.lgzarturo.springbootcourse.features.users

class PasswordEncoder {
    fun encode(rawPassword: CharSequence): String = "$rawPassword"
}
