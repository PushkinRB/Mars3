package com.roman.mars.data.repository

import com.roman.mars.data.local.dao.MessageDao
import com.roman.mars.data.local.entity.MessageEntity

class MessageRepository(
    private val messageDao: MessageDao
) {
    fun getMessagesByChat(chatId: Long) = messageDao.getMessagesByChat(chatId)

    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
    suspend fun insertMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }
    suspend fun updateMessage(messageId: Long, newText: String, newTimestamp: Long) {
        messageDao.updateMessage(messageId, newText, newTimestamp)
    }
    suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteMessage(messageId)
    }
    suspend fun getMessageCount(): Int {
        return messageDao.getMessageCount()
    }
    suspend fun clearAll() {
        messageDao.clearAll()
    }
    suspend fun deleteMessagesByChatId(chatId: Long) {
        messageDao.deleteMessagesByChatId(chatId)
    }
}