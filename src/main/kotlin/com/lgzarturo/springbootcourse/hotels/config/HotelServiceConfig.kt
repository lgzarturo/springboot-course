package com.lgzarturo.springbootcourse.hotels.config

import com.lgzarturo.springbootcourse.hotels.application.ports.output.HotelRepositoryPort
import com.lgzarturo.springbootcourse.hotels.service.HotelService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HotelServiceConfig {
    @Bean
    fun hotelService(repository: HotelRepositoryPort): HotelService = HotelService(repository)
}
