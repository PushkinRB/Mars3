package com.roman.mars.data.local.dao
import androidx.room.*
import com.roman.mars.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface MessageDao {

    // БАЗОВЫЕ ОПЕРАЦИИ
    @Query("SELECT * FROM messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    // РАБОТА С CLIENT_ID (защита от дублей)
    @Query("SELECT * FROM messages WHERE client_id = :clientId LIMIT 1")
    suspend fun getMessageByClientId(clientId: String): MessageEntity?

    // ПОЛУЧЕНИЕ НЕПРОЧИТАННЫХ
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND read_at IS NULL AND sender_id != :currentUserId")
    fun getUnreadMessages(chatId: String, currentUserId: String): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE chat_id = :chatId AND read_at IS NULL AND sender_id != :currentUserId")
    fun getUnreadCount(chatId: String, currentUserId: String): Flow<Int>

    // ОТМЕТКА КАК ПРОЧИТАННОЕ
    @Query("UPDATE messages SET read_at = :readAt WHERE id = :messageId")
    suspend fun markAsRead(messageId: String, readAt: Long)

    @Query("UPDATE messages SET read_at = :readAt WHERE chat_id = :chatId AND sender_id != :currentUserId AND read_at IS NULL")
    suspend fun markAllAsRead(chatId: String, currentUserId: String, readAt: Long)

    // СИНХРОНИЗАЦИЯ
    @Query("SELECT * FROM messages WHERE synced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>

    @Query("UPDATE messages SET synced = 1 WHERE id = :messageId")
    suspend fun markAsSynced(messageId: String)

    // ОЧИСТКА СТАРЫХ СООБЩЕНИЙ
    @Query("DELETE FROM messages WHERE chat_id = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}