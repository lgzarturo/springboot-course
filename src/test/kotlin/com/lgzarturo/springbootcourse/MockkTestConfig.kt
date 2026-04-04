package com.lgzarturo.springbootcourse

import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MockkTestConfig {
    fun mockkBean(): Any = mockk()
}
