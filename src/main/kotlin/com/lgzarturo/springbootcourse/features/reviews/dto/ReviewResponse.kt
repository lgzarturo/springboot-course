package com.lgzarturo.springbootcourse.features.reviews.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.lgzarturo.springbootcourse.features.reviews.Review
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Representación pública de una reseña")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ReviewResponse(
    @field:Schema(description = "Identificador de la reseña", example = "rev-1")
    val id: String? = null,
    @field:Schema(description = "Identificador de la reserva", example = "res-1")
    val reservationId: String = "",
    @field:Schema(description = "Identificador del hotel", example = "hotel-1")
    val hotelId: String = "",
    @field:Schema(description = "Identificador del autor", example = "1")
    val userId: Long = 0,
    @field:Schema(description = "Valoración de 1 a 5", example = "5")
    val rating: Int = 0,
    @field:Schema(description = "Comentario del huésped", example = "Estancia excelente")
    val comment: String = "",
) {
    companion object {
        fun fromDomain(review: Review): ReviewResponse =
            ReviewResponse(
                id = review.id.ifEmpty { null },
                reservationId = review.reservationId,
                hotelId = review.hotelId,
                userId = review.userId,
                rating = review.rating,
                comment = review.comment,
            )
    }
}
