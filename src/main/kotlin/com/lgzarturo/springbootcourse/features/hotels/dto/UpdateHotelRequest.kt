package com.lgzarturo.springbootcourse.features.hotels.dto

import com.lgzarturo.springbootcourse.common.constants.HotelConstants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateHotelRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(
        max = HotelConstants.NAME_FIELD_LENGTH,
        message = "El nombre no puede exceder los ${HotelConstants.NAME_FIELD_LENGTH} caracteres",
    )
    val name: String,
    @field:NotBlank(message = "La dirección no puede estar vacía")
    @field:Size(
        max = HotelConstants.ADDRESS_FIELD_LENGTH,
        message = "La dirección no puede exceder los ${HotelConstants.ADDRESS_FIELD_LENGTH} caracteres",
    )
    val address: String,
) {
    override fun toString(): String = "UpdateHotelRequest(name='$name', address='$address')"
}
