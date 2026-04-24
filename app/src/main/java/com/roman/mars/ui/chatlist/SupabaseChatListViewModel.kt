package com.roman.mars.ui.chatlist
import androidx.lifecycle.ViewModel import androidx.lifecycle.viewModelScope import com.roman.mars.data.model.Chat import com.roman.mars.data.supabase.ChatsRepository import com.roman.mars.data.supabase.MyChatRpcDto import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.coroutines.flow.StateFlow import kotlinx.coroutines.flow.asStateFlow import kotlinx.coroutines.flow.update import kotlinx.coroutines.launch import java.time.OffsetDateTime import java.time.ZoneId import java.time.format.DateTimeFormatter
class SupabaseChatListViewModel( private val chatsRepository: ChatsRepository = ChatsRepository() ) : ViewModel() {
    private val _uiState = MutableStateFlow(SupabaseChatListUiState())
    val uiState: StateFlow<SupabaseChatListUiState> = _uiState.asStateFlow()

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val rpcChats = chatsRepository.loadChats()

                val uiChats = rpcChats.map { dto -> mapToChat(dto) }

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

    private fun mapToChat(dto: MyChatRpcDto): Chat {
        val name = dto.otherUserName
            ?.takeIf { it.isNotBlank() }
            ?: dto.chatTitle
                ?.takeIf { it.isNotBlank() }
            ?: "Пользователь"

        val lastMessageTime = if (dto.lastMessageText != null && dto.chatLastMessageAt != null) {
            formatTime(dto.chatLastMessageAt)
        } else {
            ""
        }

        val lastMessage = dto.lastMessageText ?: ""

        return Chat(
            id = dto.chatId,
            name = name,
            lastMessage = lastMessage,
            time = lastMessageTime,
            unreadCount = 0,
            isLastMessageMine = false
        )
    }

    private fun formatTime(value: String): String {
        return try {
            OffsetDateTime
                .parse(value)
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(timeFormatter)
        } catch (_: Exception) {
            if (value.length >= 16) value.substring(11, 16) else ""
        }
    }

    fun deleteChat(chat: Chat) {
        _uiState.update {
            it.copy(error = "Удаление пока не реализовано")
        }
    }

    companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}