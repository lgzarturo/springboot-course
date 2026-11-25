package com.lgzarturo.springbootcourse.features.hotels

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HotelServiceConfig {
    @Bean
    fun hotelService(repository: HotelDomainRepository): HotelService = HotelService(repository)
}
