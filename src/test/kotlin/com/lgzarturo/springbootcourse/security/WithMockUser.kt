package com.lgzarturo.springbootcourse.security

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class WithMockUser(
    val userId: String = "1",
    val roles: Array<String> = ["USER"],
)
