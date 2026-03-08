package com.lgzarturo.springbootcourse.ping.config

import com.lgzarturo.springbootcourse.ping.application.ports.input.PingUseCasePort
import com.lgzarturo.springbootcourse.ping.service.PingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PingConfig {
    @Bean
    fun pingUseCase(): PingUseCasePort = PingService()
}
