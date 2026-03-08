package com.lgzarturo.springbootcourse.shared.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ObjectMapperConfig {
    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
}
