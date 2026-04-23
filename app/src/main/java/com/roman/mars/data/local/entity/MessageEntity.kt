package com.roman.mars.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: Long,
    val senderId: Long,
    val text: String = "",
val timestamp: Long,
val isIncoming: Boolean,
val status: String = MessageStatus.PENDING.name,
val type: String = MessageType.TEXT.name,
val localFilePath: String? = null,
val remoteFileUrl: String? = null,
val durationMs: Long? = null,
val transcript: String? = null
)