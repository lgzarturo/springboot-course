package com.lgzarturo.springbootcourse.features.reviews

data class Review(
    val id: String,
    val reservationId: String,
    val hotelId: String,
    val userId: Long,
    val rating: Int,
    val comment: String = "",
) {
    init {
        require(reservationId.isNotBlank()) { "La reserva no puede estar vacía" }
        require(rating in MIN_RATING..MAX_RATING) { "La valoración debe estar entre 1 y 5" }
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5

        fun averageRating(ratings: List<Int>): Double = if (ratings.isEmpty()) 0.0 else ratings.average()
    }
}
