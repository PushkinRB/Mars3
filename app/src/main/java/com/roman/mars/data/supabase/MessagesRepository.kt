package com.roman.mars.data.supabase

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.util.UUID

class MessagesRepository {

    suspend fun loadMessages(chatId: String): List<MessageDto> {
        return SupabaseProvider.client.postgrest["messages"]
            .select(
                columns = Columns.list(
                    "id", "chat_id", "sender_id", "client_id",
                    "text", "created_at", "updated_at", "deleted_at", "version"
                )
            ) {
                filter {
                    eq("chat_id", chatId)
                    isNull("deleted_at")
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MessageDto>()
    }

    fun listenForMessages(chatId: String): Flow<MessageDto?> {
        val channel = SupabaseProvider.client.realtime.channel("chat-$chatId")
        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "chat_id=eq.$chatId"
        }.map { action ->
            try {
                val record = action.record
                val deletedAt = record["deleted_at"]?.jsonPrimitive?.content
                if (!deletedAt.isNullOrBlank() && deletedAt != "null") return@map null
                MessageDto(
                    id = record["id"]?.jsonPrimitive?.content ?: return@map null,
                    chatId = record["chat_id"]?.jsonPrimitive?.content ?: return@map null,
                    senderId = record["sender_id"]?.jsonPrimitive?.content ?: return@map null,
                    clientId = record["client_id"]?.jsonPrimitive?.content,
                    text = record["text"]?.jsonPrimitive?.content ?: return@map null,
                    createdAt = record["created_at"]?.jsonPrimitive?.content ?: "",
                    updatedAt = record["updated_at"]?.jsonPrimitive?.content,
                    deletedAt = null,
                    version = record["version"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                )
            } catch (e: Exception) {
                Log.e("MessagesRepository", "Realtime parse error", e)
                null
            }
        }
    }

    suspend fun subscribeChannel(chatId: String) {
        try {
            SupabaseProvider.client.realtime.channel("chat-$chatId").subscribe()
        } catch (e: Exception) {
            Log.e("MessagesRepository", "subscribeChannel failed", e)
        }
    }

    suspend fun unsubscribeChannel(chatId: String) {
        try {
            SupabaseProvider.client.realtime.removeChannel(
                SupabaseProvider.client.realtime.channel("chat-$chatId")
            )
        } catch (e: Exception) {
            Log.e("MessagesRepository", "unsubscribeChannel failed", e)
        }
    }

    suspend fun sendMessage(chatId: String, senderId: String, text: String): MessageDto {
        return SupabaseProvider.client.postgrest["messages"]
            .insert(
                SendMessageRequest(
                    chatId = chatId,
                    senderId = senderId,
                    clientId = UUID.randomUUID().toString(),
                    text = text
                )
            ) {
                select(
                    Columns.list(
                        "id", "chat_id", "sender_id", "client_id",
                        "text", "created_at", "updated_at", "deleted_at", "version"
                    )
                )
            }
            .decodeSingle<MessageDto>()
    }

    suspend fun deleteMessage(messageId: String) {
        SupabaseProvider.client.postgrest["messages"]
            .update({ set("deleted_at", Instant.now().toString()) }) {
                filter { eq("id", messageId) }
            }
    }

    suspend fun editMessage(messageId: String, newText: String) {
        SupabaseProvider.client.postgrest["messages"]
            .update({
                set("text", newText)
                set("updated_at", Instant.now().toString())
            }) {
                filter { eq("id", messageId) }
            }
    }
}
