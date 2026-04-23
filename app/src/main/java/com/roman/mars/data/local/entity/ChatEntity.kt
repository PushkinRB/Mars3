package com.roman.mars.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val avatarRes: Int? = null,
    val lastMessagePreview: String = "",
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isLastMessageMine: Boolean = false
)