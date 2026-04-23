package com.roman.mars.data.supabase

import com.roman.mars.data.model.Message
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val marsTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun formatServerTimeToLocal(value: String): String {
    return try {
        OffsetDateTime
            .parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(marsTimeFormatter)
    } catch (_: Exception) {
        value.substring(11, 16)
    }
}

fun MessageDto.toUiMessage(currentSenderId: String): Message {
    return Message(
        id = id,
        chatId = chatId,
        text = text,
        time = formatServerTimeToLocal(createdAt),
        isMine = senderId == currentSenderId
    )
}