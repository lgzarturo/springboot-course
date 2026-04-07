package com.lgzarturo.springbootcourse.features.examples

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExampleServiceConfig(
    private val repository: ExampleRepositoryPort,
) {
    @Bean
    fun exampleUseCase(): ExampleUseCasePort = ExampleServicePort(repository = repository)
}
