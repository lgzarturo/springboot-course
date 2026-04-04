package com.lgzarturo.springbootcourse.hotels.adapters.rest.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.hotels.domain.Hotel

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class HotelResponse(
    val id: String? = null,
    val name: String = "",
    val address: String = "",
    val _links: Map<String, String> = emptyMap(),
) {
    companion object {
        fun fromDomain(hotel: Hotel): HotelResponse =
            HotelResponse(
                id = hotel.id.ifEmpty { null },
                name = hotel.name,
                address = hotel.address,
            )
    }
}
