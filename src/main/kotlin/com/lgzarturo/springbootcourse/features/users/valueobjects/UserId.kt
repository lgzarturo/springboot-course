package com.lgzarturo.springbootcourse.features.users.valueobjects

@JvmInline
value class UserId(
    val value: Long,
) {
    companion object {
        fun generate(): UserId = UserId(0L)
    }
}
