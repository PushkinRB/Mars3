package com.roman.mars.ui.chat

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object MessageTimeFormatter {

    private val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
    fun formatToLocalTime(value: String?): String {
        if (value.isNullOrBlank()) return ""
        return try {
            OffsetDateTime
                .parse(value)
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(outputFormatter)
        } catch (_: Exception) {
            value
        }
    }
}