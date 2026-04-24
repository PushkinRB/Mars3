package com.roman.mars.data.supabase

// НОВЫЙ ИМПОРТ для работы с Realtime
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.UUID

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

    // НОВАЯ ФУНКЦИЯ: Подписывается на события вставки (INSERT) в таблицу "messages"
    // Она возвращает Flow, который будет испускать новые сообщения в реальном времени
    suspend fun listenForNewMessages(chatId: String): Flow<MessageDto> {
        // Создаем канал для прослушивания изменений
        val channel = SupabaseProvider.client.realtime.createChannel("chat-$chatId")

        // Подписываемся на события вставки (INSERT) в таблице "messages" для конкретного чата
        return channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "chat_id=eq.$chatId" // Фильтруем события только для текущего чата
        }.map {
            // Как только приходит новое событие, мы декодируем его в наш объект MessageDto
            it.decodeRecord<MessageDto>()
        }.also {
            // Присоединяемся к каналу, чтобы начать слушать
            channel.join()
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

