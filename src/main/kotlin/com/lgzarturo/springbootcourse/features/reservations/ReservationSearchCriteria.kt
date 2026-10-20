package com.lgzarturo.springbootcourse.features.reservations

import java.time.LocalDate

data class ReservationSearchCriteria(
    val userId: Long? = null,
    val status: ReservationStatus? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
)
