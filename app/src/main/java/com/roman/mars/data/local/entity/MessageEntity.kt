package com.roman.mars.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "chat_id")
    val chatId: String,

    @ColumnInfo(name = "sender_id")
    val senderId: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "client_id")
    val clientId: String? = null,  // для защиты от дублей при realtime

    @ColumnInfo(name = "read_at")
    val readAt: Long? = null,  // время прочтения сообщения

    @ColumnInfo(name = "is_sent")
    val isSent: Boolean = true,  // отправлено ли на сервер

    @ColumnInfo(name = "synced")
    val synced: Boolean = false  // синхронизировано ли с Supabase
)