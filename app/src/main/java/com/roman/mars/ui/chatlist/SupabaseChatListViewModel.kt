package com.roman.mars.ui.chatlist
import androidx.lifecycle.ViewModel import androidx.lifecycle.viewModelScope import com.roman.mars.data.model.Chat import com.roman.mars.data.supabase.ChatsRepository import com.roman.mars.data.supabase.MyChatRpcDto import com.roman.mars.ui.auth.SessionRepository import kotlinx.coroutines.flow.MutableStateFlow import kotlinx.coroutines.flow.StateFlow import kotlinx.coroutines.flow.asStateFlow import kotlinx.coroutines.flow.update import kotlinx.coroutines.launch import java.time.OffsetDateTime import java.time.ZoneId import java.time.format.DateTimeFormatter
class SupabaseChatListViewModel( private val chatsRepository: ChatsRepository = ChatsRepository(), private val sessionRepository: SessionRepository = SessionRepository() ) : ViewModel() {
    private val _uiState = MutableStateFlow(SupabaseChatListUiState())
    val uiState: StateFlow<SupabaseChatListUiState> = _uiState.asStateFlow()

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val currentUserId = sessionRepository.currentUserId().orEmpty()
                val rpcChats = chatsRepository.loadChats()

                val uiChats = rpcChats
                    .distinctBy { it.chatId }
                    .map { dto -> mapToChat(dto, currentUserId) }

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

    private fun mapToChat(dto: MyChatRpcDto, currentUserId: String): Chat {
        val name = dto.chatTitle
            ?.takeIf { it.isNotBlank() }
            ?: dto.otherUserName
                ?.takeIf { it.isNotBlank() && !it.contains("@") }
            ?: "Пользователь"

        return Chat(
            id = dto.chatId,
            name = name,
            lastMessage = dto.lastMessageText ?: "",
            time = dto.chatLastMessageAt?.let(::formatTime) ?: "",
            unreadCount = 0,
            isLastMessageMine = dto.lastMessageSenderId == currentUserId
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

    companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}