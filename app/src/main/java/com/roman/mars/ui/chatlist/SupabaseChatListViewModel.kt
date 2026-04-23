package com.roman.mars.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.model.Chat
import com.roman.mars.data.supabase.ChatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SupabaseChatListViewModel(
    private val chatsRepository: ChatsRepository = ChatsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupabaseChatListUiState())
    val uiState: StateFlow<SupabaseChatListUiState> = _uiState.asStateFlow()

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isLoading = true, error = null)
            }

            try {
                val rpcChats = chatsRepository.loadChats()

                val uiChats = rpcChats.map { dto ->
                    Chat(
                        id = dto.chatId,
                        name = dto.otherUserName
                            ?.takeIf { it.isNotBlank() }
                            ?: dto.chatTitle
                                ?.takeIf { it.isNotBlank() }
                            ?: "Пользователь",
                        lastMessage = "",
                        time = formatTime(dto.chatLastMessageAt ?: dto.chatCreatedAt),
                        unreadCount = 0,
                        isLastMessageMine = false
                    )
                }

                _uiState.update { current ->
                    current.copy(
                        chats = uiChats,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить чаты"
                    )
                }
            }
        }
    }

    fun deleteChat(chat: Chat) {
        _uiState.update { current ->
            current.copy(
                error = "Удаление чата пока отключено. Сначала переведём архитектуру на безопасный путь."
            )
        }
    }

    private fun formatTime(value: String): String {
        return if (value.length >= 16 && value.contains("T")) {
            value.substring(11, 16)
        } else {
            ""
        }
    }
}
