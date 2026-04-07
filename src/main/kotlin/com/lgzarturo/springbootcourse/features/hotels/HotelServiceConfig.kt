package com.lgzarturo.springbootcourse.features.hotels

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HotelServiceConfig {
    @Bean
    fun hotelService(repository: HotelRepositoryPort): HotelService = HotelService(repository)
}
