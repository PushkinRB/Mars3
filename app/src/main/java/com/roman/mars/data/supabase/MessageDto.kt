package com.roman.mars.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String,
@SerialName("chat_id") val chatId: String,
@SerialName("sender_id") val senderId: String,
@SerialName("client_id") val clientId: String? = null,
    val text: String,
@SerialName("created_at") val createdAt: String,
@SerialName("updated_at") val updatedAt: String? = null,
@SerialName("deleted_at") val deletedAt: String? = null,
    val version: Int = 1
)
