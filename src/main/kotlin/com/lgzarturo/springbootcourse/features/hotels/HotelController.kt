package com.lgzarturo.springbootcourse.features.hotels

import com.lgzarturo.springbootcourse.features.hotels.dto.CreateHotelRequest
import com.lgzarturo.springbootcourse.features.hotels.dto.HotelResponse
import com.lgzarturo.springbootcourse.features.hotels.dto.PageResponse
import com.lgzarturo.springbootcourse.features.hotels.dto.UpdateHotelRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/hotels")
class HotelController(
    private val hotelService: HotelService,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateHotelRequest,
    ): ResponseEntity<HotelResponse> {
        val hotel = Hotel(id = "", name = request.name, address = request.address)
        val savedHotel = hotelService.createHotel(hotel)
        return ResponseEntity.status(HttpStatus.CREATED).body(HotelResponse.fromDomain(savedHotel))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: String,
    ): ResponseEntity<HotelResponse> {
        val hotel = hotelService.getHotelById(id)
        return hotel
            ?.let { ResponseEntity.ok(HotelResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping
    fun getAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<PageResponse<HotelResponse>> {
        val criteria = HotelSearchCriteria(name = name)
        val (hotels, total) = hotelService.getAllHotels(criteria, page, size)
        val hotelResponses = hotels.map { HotelResponse.fromDomain(it) }
        val pageResponse = PageResponse.from(hotelResponses, total, page, size)
        return ResponseEntity.ok(pageResponse)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateHotelRequest,
    ): ResponseEntity<HotelResponse> {
        val hotel = Hotel(id = id, name = request.name, address = request.address)
        val updatedHotel = hotelService.updateHotel(id, hotel)
        return updatedHotel
            ?.let { ResponseEntity.ok(HotelResponse.fromDomain(it)) }
            ?: ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String,
    ): ResponseEntity<Void> {
        val deleted = hotelService.deleteHotel(id)
        return if (deleted) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
