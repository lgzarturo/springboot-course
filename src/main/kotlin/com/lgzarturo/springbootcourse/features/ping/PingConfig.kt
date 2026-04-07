package com.lgzarturo.springbootcourse.features.ping

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PingConfig {
    @Bean
    fun pingUseCase(): PingUseCasePort = PingService()
}
