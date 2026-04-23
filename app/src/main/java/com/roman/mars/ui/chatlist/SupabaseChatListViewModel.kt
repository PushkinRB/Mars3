package com.roman.mars.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.model.Chat
import com.roman.mars.data.supabase.ChatsRepository
import com.roman.mars.data.supabase.MessagesRepository
import com.roman.mars.ui.auth.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupabaseChatListViewModel(
    private val chatsRepository: ChatsRepository = ChatsRepository(),
    private val messagesRepository: MessagesRepository = MessagesRepository(),
    private val sessionRepository: SessionRepository = SessionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupabaseChatListUiState())
    val uiState: StateFlow<SupabaseChatListUiState> = _uiState.asStateFlow()
    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentSenderId = sessionRepository.currentUserId()
                val chatDtos = chatsRepository.loadChats()
                val uiChats = chatDtos.map { dto ->
                    val messages = messagesRepository.loadMessages(dto.id)
                    val lastMessage = messages.lastOrNull()
                    Chat(
                        id = dto.id,
                        name = dto.title ?: "Новый чат",
                        lastMessage = lastMessage?.text ?: "",
                        time = lastMessage?.createdAt?.substring(11, 16) ?: "",
                        unreadCount = 0,
                        isLastMessageMine = lastMessage?.senderId == currentSenderId
                    )
                }
                _uiState.update {
                    it.copy(
                        chats = uiChats,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить чаты"
                    )
                }
            }
        }
    }
    fun createChat() {
        viewModelScope.launch {
            try {
                val createdBy = sessionRepository.currentUserId()
                if (createdBy.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(error = "Пользователь не авторизован")
                    }
                    return@launch
                }
                val index = uiState.value.chats.size + 1
                chatsRepository.createChat(
                    title = "Новый чат $index",
                    createdBy = createdBy
                )
                loadChats()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось создать чат")
                }
            }
        }
    }
    fun assignContactToChat(chatId: String, contactName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                chatsRepository.updateChatTitle(chatId, contactName)
                loadChats()
                onDone()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось назначить контакт")
                }
            }
        }
    }
    fun deleteChat(chat: Chat) {
        viewModelScope.launch {
            try {
                chatsRepository.deleteChat(chat.id)
                loadChats()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось удалить чат")
                }
            }
        }
    }
}