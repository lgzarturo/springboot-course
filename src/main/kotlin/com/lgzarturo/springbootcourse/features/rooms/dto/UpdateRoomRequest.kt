package com.lgzarturo.springbootcourse.features.rooms.dto

import com.lgzarturo.springbootcourse.features.rooms.PokemonTheme
import com.lgzarturo.springbootcourse.features.rooms.RoomStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

@Schema(description = "Datos para actualizar una habitación")
data class UpdateRoomRequest(
    @field:NotBlank(message = "El número no puede estar vacío")
    @field:Size(max = 50, message = "El número no puede exceder los 50 caracteres")
    @field:Schema(description = "Número de la habitación", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    val number: String,
    @field:NotBlank(message = "El tipo no puede estar vacío")
    @field:Size(max = 50, message = "El tipo no puede exceder los 50 caracteres")
    @field:Schema(description = "Tipo de habitación", example = "SUITE", requiredMode = Schema.RequiredMode.REQUIRED)
    val type: String,
    @field:Schema(
        description = "Estado de la habitación",
        example = "AVAILABLE",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val status: RoomStatus,
    @field:Schema(
        description = "Tema Pokémon de la habitación",
        example = "PIKACHU",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val theme: PokemonTheme,
    @field:Positive(message = "La capacidad debe ser positiva")
    @field:Schema(description = "Capacidad de huéspedes", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    val capacity: Int,
    @field:PositiveOrZero(message = "El precio no puede ser negativo")
    @field:Schema(description = "Precio por noche", example = "150.0", requiredMode = Schema.RequiredMode.REQUIRED)
    val price: Double,
    @field:Size(max = 2000, message = "La descripción no puede exceder los 2000 caracteres")
    @field:Schema(description = "Descripción de la habitación", example = "Suite con vista al lago")
    val description: String? = null,
)
