package com.lgzarturo.springbootcourse.shared.extension

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Formatea un LocalDateTime a un String con formato ISO
 */
fun LocalDateTime.toIsoString(): String = this.format(DateTimeFormatter.ISO_DATE_TIME)

/**
 * Formatea un LocalDateTime a un String con formato personalizado
 */
fun LocalDateTime.toFormattedString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String =
    this
        .format(
            DateTimeFormatter.ofPattern(pattern),
        )
