package com.roman.mars.data.supabase
import android.util.Log
import com.roman.mars.data.local.dao.MessageDao
import com.roman.mars.data.local.entity.MessageEntity
import com.roman.mars.data.supabase.models.MessageDTO
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID
class MessagesRepository(
    private val messageDao: MessageDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val client = SupabaseProvider.client
    private val channels = mutableMapOf<String, io.github.jan.supabase.realtime.RealtimeChannel>()
    // 1. ПОЛУЧЕНИЕ СООБЩЕНИЙ
    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        scope.launch { syncMessagesFromSupabase(chatId) }
        return messageDao.getMessagesByChatId(chatId)
    }
    // 2. ОТПРАВКА СООБЩЕНИЯ
    suspend fun sendMessage(
        chatId: String,
        content: String,
        senderId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val clientId = UUID.randomUUID().toString()
            val newMessage = MessageDTO(
                id = UUID.randomUUID().toString(),
                chat_id = chatId,
                sender_id = senderId,
                content = content,
                client_id = clientId,
                created_at = null
            )
            // Отправляем в Supabase
            client.from("messages").insert(newMessage)
            // Сохраняем локально (с client_id для защиты от дублей)
            val localMessage = MessageEntity(
                id = newMessage.id,
                chatId = chatId,
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis(),
                clientId = clientId
            )
            messageDao.insertMessage(localMessage)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MessagesRepository", "Send failed", e)
            Result.failure(e)
        }
    }
    // 3. REALTIME ПОДПИСКА
    fun subscribeToMessages(chatId: String) {
        if (channels.containsKey(chatId)) return
        val channel = client.realtime.channel("messages_$chatId")

        channel.postgresChangeFlow<PostgresAction>("public") {
            table = "messages"
            filter = "chat_id=eq.$chatId"
        }.onEach { action ->
            when (action) {
                is PostgresAction.Insert -> {
                    val dto = action.decodeRecord<MessageDTO>()
                    saveMessageLocally(dto)
                }
                is PostgresAction.Update -> {
                    val dto = action.decodeRecord<MessageDTO>()
                    updateMessageLocally(dto)
                }
                is PostgresAction.Delete -> {
                    val messageId = action.oldRecord["id"] as? String
                    messageId?.let { messageDao.deleteMessageById(it) }
                }
            }
        }.catch { e ->
            Log.e("Realtime", "Error: ${e.message}")
        }.launchIn(scope)
        scope.launch {
            channel.subscribe()
        }
        channels[chatId] = channel
    }
    // 4. ОТПИСКА ОТ REALTIME
    suspend fun unsubscribeFromMessages(chatId: String) {
        channels[chatId]?.let {
            it.unsubscribe()
            channels.remove(chatId)
        }
    }
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    private suspend fun syncMessagesFromSupabase(chatId: String) {
        try {
            val messages = client.from("messages")
                .select()
                .decodeList<MessageDTO>()
                .filter { it.chat_id == chatId }
            messages.forEach { saveMessageLocally(it) }
        } catch (e: Exception) {
            Log.e("Sync", "Failed to sync messages", e)
        }
    }
    private suspend fun saveMessageLocally(dto: MessageDTO) {
        // Защита от дублей по client_id
        val exists = messageDao.getMessageByClientId(dto.client_id ?: "") != null
        if (exists) return
        val entity = MessageEntity(
            id = dto.id,
            chatId = dto.chat_id,
            senderId = dto.sender_id,
            content = dto.content,
            timestamp = parseTimestamp(dto.created_at),
            clientId = dto.client_id
        )
        messageDao.insertMessage(entity)
    }
    private suspend fun updateMessageLocally(dto: MessageDTO) {
        val entity = messageDao.getMessageById(dto.id) ?: return
        messageDao.updateMessage(entity.copy(
            content = dto.content,
            readAt = dto.read_at
        ))
    }
    private fun parseTimestamp(isoString: String?): Long {
        return try {
            isoString?.let {
                java.time.Instant.parse(it).toEpochMilli()
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}