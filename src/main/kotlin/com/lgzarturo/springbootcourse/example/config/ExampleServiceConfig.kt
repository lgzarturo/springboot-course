package com.lgzarturo.springbootcourse.example.config

import com.lgzarturo.springbootcourse.example.application.ports.input.ExampleUseCasePort
import com.lgzarturo.springbootcourse.example.application.ports.output.ExampleRepositoryPort
import com.lgzarturo.springbootcourse.example.service.ExampleServicePort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExampleServiceConfig(
    private val repository: ExampleRepositoryPort,
) {
    @Bean
    fun exampleUseCase(): ExampleUseCasePort = ExampleServicePort(repository = repository)
}
