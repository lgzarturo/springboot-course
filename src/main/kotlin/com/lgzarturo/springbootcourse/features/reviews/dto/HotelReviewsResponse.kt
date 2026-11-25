package com.lgzarturo.springbootcourse.features.reviews.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Resumen paginado de reseñas de un hotel con su valoración media")
data class HotelReviewsResponse(
    @field:Schema(description = "Reseñas de la página actual")
    val content: List<ReviewResponse> = emptyList(),
    @field:Schema(description = "Total de reseñas en todas las páginas", example = "42")
    val totalElements: Long = 0,
    @field:Schema(description = "Total de páginas disponibles", example = "5")
    val totalPages: Int = 0,
    @field:Schema(description = "Indica si es la primera página", example = "true")
    val first: Boolean = false,
    @field:Schema(description = "Indica si es la última página", example = "false")
    val last: Boolean = false,
    @field:Schema(description = "Número de página actual (base 0)", example = "0")
    val number: Int = 0,
    @field:Schema(description = "Tamaño de página solicitado", example = "10")
    val size: Int = 0,
    @field:Schema(description = "Valoración media del hotel (0.0 sin reseñas)", example = "4.5")
    val averageRating: Double = 0.0,
) {
    companion object {
        fun from(
            items: List<ReviewResponse>,
            total: Long,
            page: Int,
            size: Int,
            average: Double,
        ): HotelReviewsResponse {
            val totalPages = if (size > 0) ((total + size - 1) / size).toInt() else 0
            return HotelReviewsResponse(
                content = items,
                totalElements = total,
                totalPages = totalPages,
                first = page == 0,
                last = page >= totalPages - 1 || totalPages == 0,
                number = page,
                size = size,
                averageRating = average,
            )
        }
    }
}
