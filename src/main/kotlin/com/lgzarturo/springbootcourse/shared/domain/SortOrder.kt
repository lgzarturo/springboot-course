package com.lgzarturo.springbootcourse.shared.domain

data class SortOrder(
    val property: String,
    val direction: Direction,
) {
    enum class Direction { ASC, DESC }
}
