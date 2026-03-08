package com.lgzarturo.springbootcourse.shared.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

@TestConfiguration
class ObjectMapperConfig {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}
