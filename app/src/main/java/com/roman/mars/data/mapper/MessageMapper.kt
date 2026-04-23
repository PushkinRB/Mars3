package com.roman.mars.data.mapper

import com.roman.mars.data.local.entity.MessageEntity
import com.roman.mars.data.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun MessageEntity.toUiMessage(): Message {
    return Message(
        id = id.toString(),
        chatId = chatId.toString(),
        text = text,
        time = formatTimestamp(timestamp),
        isMine = !isIncoming
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}