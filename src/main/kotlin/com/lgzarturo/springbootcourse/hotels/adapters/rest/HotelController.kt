@file:Suppress("EmptyClassBlock")

package com.lgzarturo.springbootcourse.hotels.adapters.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/hotels")
class HotelController {
    @GetMapping
    fun getAll() = listOf<String>()
}
