package com.lgzarturo.springbootcourse.features.rooms.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.features.rooms.PokemonTheme
import com.lgzarturo.springbootcourse.features.rooms.Room
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de una habitación")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class RoomResponse(
    @field:Schema(description = "Identificador de la habitación", example = "1")
    val id: String? = null,
    @field:Schema(description = "Número de la habitación", example = "101")
    val number: String = "",
    @field:Schema(description = "Tipo de habitación", example = "SUITE")
    val type: String = "",
    @field:Schema(description = "Estado de la habitación", example = "AVAILABLE")
    val status: RoomStatus = RoomStatus.AVAILABLE,
    @field:Schema(description = "Tema Pokémon de la habitación", example = "PIKACHU")
    val theme: PokemonTheme = PokemonTheme.PIKACHU,
    @field:Schema(description = "Capacidad de huéspedes", example = "2")
    val capacity: Int = 0,
    @field:Schema(description = "Descripción de la habitación", example = "Suite con vista al lago")
    val description: String? = null,
    @field:Schema(description = "Precio por noche", example = "150.0")
    val price: Double = 0.0,
    @field:Schema(description = "Identificador del hotel", example = "1")
    val hotelId: String = "",
) {
    companion object {
        fun fromDomain(room: Room): RoomResponse =
            RoomResponse(
                id = room.id.ifEmpty { null },
                number = room.number,
                type = room.type,
                status = room.status,
                theme = room.theme,
                capacity = room.capacity,
                description = room.description,
                price = room.price,
                hotelId = room.hotelId,
            )
    }
}
