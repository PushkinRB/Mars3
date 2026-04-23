package com.roman.mars.data.mapper
import com.roman.mars.data.local.entity.ChatEntity
import com.roman.mars.data.model.Chat
fun ChatEntity.toChat(): Chat {
    return Chat(
        id = id.toString(),
        name = title,
        lastMessage = lastMessagePreview,
        time = formatChatTime(lastMessageTimestamp),
        unreadCount = unreadCount,
        isLastMessageMine = isLastMessageMine
    )
}