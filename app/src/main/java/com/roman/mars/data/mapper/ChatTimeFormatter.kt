package com.roman.mars.data.mapper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatChatTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""

    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}