package com.roman.mars.presentation.chatlist
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.local.entity.ChatEntity
import com.roman.mars.data.repository.ChatRepository
import com.roman.mars.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
class ChatListViewModel(
    private val repository: ChatRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {
    private val _chats = MutableStateFlow<List<ChatEntity>>(emptyList())
    val chats: StateFlow<List<ChatEntity>> = _chats.asStateFlow()
    init {
        viewModelScope.launch {
            repository.getAllChats().collectLatest { chatList ->
                _chats.value = chatList
            }
        }
    }
    fun insertInitialChatsIfEmpty() {
        viewModelScope.launch {
            val count = repository.getChatCount()
            if (count == 0) {
                repository.insertChats(
                    listOf(
                        ChatEntity(
                            id = 1L,
                            title = "Алексей",
                            lastMessagePreview = "На связи",
                            lastMessageTimestamp = System.currentTimeMillis()
                        ),
                        ChatEntity(
                            id = 2L,
                            title = "Сергей",
                            lastMessagePreview = "Как обстановка?",
                            lastMessageTimestamp = System.currentTimeMillis() - 60000
                        ),
                        ChatEntity(
                            id = 3L,
                            title = "Бригада Север",
                            lastMessagePreview = "Смена началась",
                            lastMessageTimestamp = System.currentTimeMillis() - 120000
                        )
                    )
                )
            }
        }
    }
    fun createChatIfNotExists(title: String, onChatReady: (ChatEntity) -> Unit) {
        viewModelScope.launch {
            val existingChat = repository.getChatByTitle(title)
            if (existingChat != null) {
                onChatReady(existingChat)
            } else {
                val newChat = ChatEntity(
                    id = System.currentTimeMillis(),
                    title = title,
                    lastMessagePreview = "",
                    lastMessageTimestamp = 0L,
                    unreadCount = 0
                )
                repository.insertChat(newChat)
                onChatReady(newChat)
            }
        }
    }
    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            messageRepository.deleteMessagesByChatId(chatId)
            repository.deleteChatById(chatId)
        }
    }
}