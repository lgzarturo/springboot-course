package com.lgzarturo.springbootcourse.hotels.integration.e2e

import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RestTestClientConfig {
    @Bean
    fun testRestTemplate(): TestRestTemplate = TestRestTemplate()
}
