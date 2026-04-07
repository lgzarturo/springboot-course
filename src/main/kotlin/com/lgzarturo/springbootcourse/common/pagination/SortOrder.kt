package com.lgzarturo.springbootcourse.common.pagination

data class SortOrder(
    val property: String,
    val direction: Direction,
) {
    enum class Direction { ASC, DESC }
}
