package com.roman.mars.presentation.chat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.local.entity.MessageEntity
import com.roman.mars.data.repository.ChatRepository
import com.roman.mars.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class MessageViewModel(
    private val repository: MessageRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()
    private var currentChatId: Long = 0L
    fun selectChat(chatId: Long) {
        currentChatId = chatId
        viewModelScope.launch {
            repository.getMessagesByChat(chatId).collectLatest { messageList ->
                _messages.value = messageList
            }
        }
    }
    fun sendMessage(text: String) {
        if (text.isBlank() || currentChatId == 0L) return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            repository.insertMessage(
                MessageEntity(
                    chatId = currentChatId,
                    senderId = 1L,
                    text = text,
                    timestamp = timestamp,
                    isIncoming = false
                )
            )
            chatRepository.updateLastMessage(
                chatId = currentChatId,
                lastMessage = text,
                timestamp = timestamp,
                isLastMessageMine = true
            )
        }
    }
    fun editMessage(messageId: Long, newText: String) {
        if (newText.isBlank()) return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            repository.updateMessage(messageId, newText, timestamp)
            val lastMessage = _messages.value.lastOrNull()
            if (lastMessage != null && lastMessage.id == messageId) {
                chatRepository.updateLastMessage(
                    chatId = currentChatId,
                    lastMessage = newText,
                    timestamp = timestamp,
                    isLastMessageMine = !lastMessage.isIncoming
                )
            }
        }
    }
    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            val deletingMessage = _messages.value.find { it.id == messageId }
            repository.deleteMessage(messageId)
            val remainingMessages = _messages.value.filter { it.id != messageId }
            val newLastMessage = remainingMessages.lastOrNull()
            if (deletingMessage != null && deletingMessage.chatId == currentChatId) {
                chatRepository.updateLastMessage(
                    chatId = currentChatId,
                    lastMessage = newLastMessage?.text ?: "",
                    timestamp = newLastMessage?.timestamp ?: 0L,
                    isLastMessageMine = newLastMessage?.let { !it.isIncoming } ?: false
                )
            }
        }
    }
    fun insertInitialMessagesIfEmpty() {
        viewModelScope.launch {
            val messageCount = repository.getMessageCount()
            if (messageCount == 0) {
                repository.insertMessages(
                    listOf(
                        MessageEntity(
                            chatId = 1L,
                            senderId = 2L,
                            text = "Доброе утро, как смена?",
                            timestamp = System.currentTimeMillis() - 3600000,
                            isIncoming = true
                        ),
                        MessageEntity(
                            chatId = 2L,
                            senderId = 2L,
                            text = "Проверь связь на участке",
                            timestamp = System.currentTimeMillis() - 3000000,
                            isIncoming = true
                        ),
                        MessageEntity(
                            chatId = 3L,
                            senderId = 2L,
                            text = "Все на месте, начинаем работу",
                            timestamp = System.currentTimeMillis() - 2400000,
                            isIncoming = true
                        )
                    )
                )
            }
        }
    }
}