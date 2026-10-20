package com.lgzarturo.springbootcourse.features.hotels.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.features.hotels.Hotel
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de un hotel")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class HotelResponse(
    @field:Schema(description = "Identificador del hotel", example = "1")
    val id: String? = null,
    @field:Schema(description = "Nombre del hotel", example = "Gran Hotel Pikachu")
    val name: String = "",
    @field:Schema(description = "Dirección del hotel", example = "Calle Trueno 25, Ciudad Carmín")
    val address: String = "",
    @field:Schema(description = "Enlaces HATEOAS del recurso, si aplica")
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
