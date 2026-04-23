package com.roman.mars.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id") val chatId: String,
@SerialName("sender_id") val senderId: String,
@SerialName("client_id") val clientId: String,
val text: String
)