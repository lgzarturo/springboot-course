package com.lgzarturo.springbootcourse.config.metrics

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Registra el interceptor de métricas de reservas en las rutas del recurso.
 */
@Configuration
class ReservationMetricsConfig(
    private val reservationMetricsInterceptor: ReservationMetricsInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry
            .addInterceptor(reservationMetricsInterceptor)
            .addPathPatterns("/api/v1/reservations", "/api/v1/reservations/**")
    }
}
