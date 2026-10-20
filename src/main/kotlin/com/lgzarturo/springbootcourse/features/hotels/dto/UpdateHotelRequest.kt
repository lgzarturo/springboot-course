package com.lgzarturo.springbootcourse.features.hotels.dto

import com.lgzarturo.springbootcourse.common.constants.HotelConstants
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Datos para actualizar un hotel")
data class UpdateHotelRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(
        max = HotelConstants.NAME_FIELD_LENGTH,
        message = "El nombre no puede exceder los ${HotelConstants.NAME_FIELD_LENGTH} caracteres",
    )
    @field:Schema(
        description = "Nombre del hotel",
        maxLength = HotelConstants.NAME_FIELD_LENGTH,
        example = "Gran Hotel Pikachu",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val name: String,
    @field:NotBlank(message = "La dirección no puede estar vacía")
    @field:Size(
        max = HotelConstants.ADDRESS_FIELD_LENGTH,
        message = "La dirección no puede exceder los ${HotelConstants.ADDRESS_FIELD_LENGTH} caracteres",
    )
    @field:Schema(
        description = "Dirección del hotel",
        maxLength = HotelConstants.ADDRESS_FIELD_LENGTH,
        example = "Calle Trueno 25, Ciudad Carmín",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val address: String,
) {
    override fun toString(): String = "UpdateHotelRequest(name='$name', address='$address')"
}
