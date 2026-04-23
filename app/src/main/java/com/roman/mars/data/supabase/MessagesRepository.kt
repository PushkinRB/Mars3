package com.roman.mars.data.supabase
import io.github.jan.supabase.postgrest.postgrest import io.github.jan.supabase.postgrest.query.Columns import io.github.jan.supabase.postgrest.query.Order import java.time.Instant import java.util.UUID
class MessagesRepository {
    suspend fun loadMessages(chatId: String): List<MessageDto> {
        return SupabaseProvider.client.postgrest["messages"]
            .select(
                columns = Columns.list(
                    "id",
                    "chat_id",
                    "sender_id",
                    "client_id",
                    "text",
                    "created_at",
                    "updated_at",
                    "deleted_at",
                    "version"
                )
            ) {
                filter {
                    eq("chat_id", chatId)
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MessageDto>()
            .filter { it.deletedAt == null }
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
                        "id",
                        "chat_id",
                        "sender_id",
                        "client_id",
                        "text",
                        "created_at",
                        "updated_at",
                        "deleted_at",
                        "version"
                    )
                )
            }
            .decodeSingle<MessageDto>()
    }

    suspend fun deleteMessage(messageId: String) {
        SupabaseProvider.client.postgrest["messages"]
            .update(
                {
                    set("deleted_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", messageId)
                }
            }
    }

    suspend fun editMessage(messageId: String, newText: String) {
        SupabaseProvider.client.postgrest["messages"]
            .update(
                {
                    set("text", newText)
                    set("updated_at", Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", messageId)
                }
            }
    }
}