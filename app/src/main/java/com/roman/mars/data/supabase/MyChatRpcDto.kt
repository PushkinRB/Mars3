package com.roman.mars.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChatRpcDto(
   @SerialName("chat_id")
   val chatId: String,
@SerialName("chat_type")
val chatType: String,
@SerialName("chat_title")
val chatTitle: String? = null,
@SerialName("chat_created_at")
val chatCreatedAt: String,
@SerialName("chat_updated_at")
val chatUpdatedAt: String,
@SerialName("chat_last_message_at")
val chatLastMessageAt: String? = null,
@SerialName("last_message_text")
val lastMessageText: String? = null,
@SerialName("last_message_sender_id")
val lastMessageSenderId: String? = null,
@SerialName("other_user_id")
val otherUserId: String? = null,
@SerialName("other_user_name")
val otherUserName: String? = null,
@SerialName("other_user_phone")
val otherUserPhone: String? = null
)