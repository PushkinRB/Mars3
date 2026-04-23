package com.roman.mars.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roman.mars.data.local.entity.ChatEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastMessageTimestamp DESC, title ASC")
    fun getAllChats(): Flow<List<ChatEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    @Query("UPDATE chats SET lastMessagePreview = :lastMessage, lastMessageTimestamp = :timestamp, isLastMessageMine = :isLastMessageMine WHERE id = :chatId")
    suspend fun updateLastMessage(
        chatId: Long,
        lastMessage: String,
        timestamp: Long,
        isLastMessageMine: Boolean
    )
    @Query("SELECT COUNT(*) FROM chats")
    suspend fun getChatCount(): Int
    @Query("SELECT * FROM chats WHERE title = :title LIMIT 1")
    suspend fun getChatByTitle(title: String): ChatEntity?
    @Query("DELETE FROM chats")
    suspend fun clearAll()
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: Long)
}