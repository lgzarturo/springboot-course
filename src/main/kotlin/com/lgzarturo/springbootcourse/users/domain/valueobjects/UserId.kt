package com.lgzarturo.springbootcourse.users.domain.valueobjects

@JvmInline
value class UserId(
    val value: Long,
) {
    companion object {
        fun generate(): UserId = UserId(0L)
    }
}
