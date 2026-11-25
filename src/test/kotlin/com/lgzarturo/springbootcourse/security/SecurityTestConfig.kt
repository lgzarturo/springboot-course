package com.lgzarturo.springbootcourse.security

import com.lgzarturo.springbootcourse.config.security.JwtService
import com.lgzarturo.springbootcourse.features.auth.TokenRevocationPort
import com.lgzarturo.springbootcourse.features.users.UserDomainRepository
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

    // Dependencias del filtro JWT (parseToken relajado retorna null y el filtro
    // no las toca en slices, pero deben existir para construir el bean).
    @Bean
    fun tokenRevocationPort(): TokenRevocationPort = mockk(relaxed = true)

    @Bean
    fun userDomainRepository(): UserDomainRepository = mockk(relaxed = true)

    // ReservationMetricsConfig (WebMvcConfigurer) se carga en @WebMvcTest y
    // requiere MeterRegistry, que no se autoconfigura en slices web.
    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
}
