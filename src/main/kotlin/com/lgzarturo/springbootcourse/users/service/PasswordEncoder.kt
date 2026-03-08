package com.lgzarturo.springbootcourse.users.service

class PasswordEncoder {
    fun encode(rawPassword: CharSequence): String = "$rawPassword"
}
