package com.roman.mars.data.repository
import com.roman.mars.data.local.dao.ChatDao
import com.roman.mars.data.local.entity.ChatEntity
class ChatRepository(
    private val chatDao: ChatDao
) {
    fun getAllChats() = chatDao.getAllChats()
    suspend fun insertChat(chat: ChatEntity) {
        chatDao.insertChat(chat)
    }
    suspend fun insertChats(chats: List<ChatEntity>) {
        chatDao.insertChats(chats)
    }
    suspend fun updateLastMessage(
        chatId: Long,
        lastMessage: String,
        timestamp: Long,
        isLastMessageMine: Boolean
    ) {
        chatDao.updateLastMessage(chatId, lastMessage, timestamp, isLastMessageMine)
    }
    suspend fun getChatCount(): Int {
        return chatDao.getChatCount()
    }
    suspend fun getChatByTitle(title: String): ChatEntity? {
        return chatDao.getChatByTitle(title)
    }
    suspend fun clearAll() {
        chatDao.clearAll()
    }
    suspend fun deleteChatById(chatId: Long) {
        chatDao.deleteChatById(chatId)
    }
}