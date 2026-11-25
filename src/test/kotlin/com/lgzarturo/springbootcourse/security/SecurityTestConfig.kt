package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.config.security.JwtService
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class SecurityTestConfig {
    // Los slices @WebMvcTest incluyen el filtro JWT real pero no la cadena de
    // seguridad ni JwtService: se sustituye por un mock que deja pasar todo.
    @Bean
    fun jwtService(): JwtService = mockk(relaxed = true)

    // ReservationMetricsConfig (WebMvcConfigurer) se carga en @WebMvcTest y
    // requiere MeterRegistry, que no se autoconfigura en slices web.
    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
}
