package com.roman.mars.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
    val id: String,
    val type: String,
    val title: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
@SerialName("created_at") val createdAt: String,
@SerialName("updated_at") val updatedAt: String,
@SerialName("last_message_id") val lastMessageId: String? = null,
@SerialName("last_message_at") val lastMessageAt: String? = null
)