package com.roman.mars.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.supabase.MessageDto
import com.roman.mars.data.supabase.MessagesRepository
import com.roman.mars.ui.auth.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: MessagesRepository = MessagesRepository(),
    private val sessionRepository: SessionRepository = SessionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(messages = emptyList()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null
    private var realtimeJob: Job? = null

    // Устанавливаем чат — вызывается при открытии экрана чата
    fun setChat(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        loadMessages()
        startRealtime(chatId)
    }

    // Загрузка сообщений из базы (первичная и после действий)
    fun loadMessages() {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val messages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(messages = messages, isLoading = false, error = null)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "loadMessages failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить сообщения"
                    )
                }
            }
        }
    }

    // Запускаем Realtime-подписку вместо polling
    private fun startRealtime(chatId: String) {
        realtimeJob?.cancel()
        realtimeJob = viewModelScope.launch {
            try {
                // Подписываемся на канал
                repository.subscribeChannel(chatId)

                // Слушаем новые сообщения
                repository.listenForMessages(chatId)
                    .filterNotNull()
                    .collect { newMessage ->
                        _uiState.update { state ->
                            // Защита от дублей по client_id и id
                            val alreadyExists = state.messages.any { existing ->
                                existing.id == newMessage.id ||
                                        (!newMessage.clientId.isNullOrBlank() &&
                                                existing.clientId == newMessage.clientId)
                            }
                            if (alreadyExists) {
                                state
                            } else {
                                state.copy(messages = state.messages + newMessage)
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Realtime failed, falling back", e)
                // Если Realtime не работает (слабая сеть) — не крашимся
            }
        }
    }

    override fun onCleared() {
        realtimeJob?.cancel()
        viewModelScope.launch {
            currentChatId?.let { repository.unsubscribeChannel(it) }
        }
        super.onCleared()
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draftMessage = value) }
    }

    fun startEditing(messageId: String, oldText: String) {
        _uiState.update {
            it.copy(
                draftMessage = oldText,
                editingMessageId = messageId,
                isEditing = true,
                error = null
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                draftMessage = "",
                editingMessageId = null,
                isEditing = false,
                error = null
            )
        }
    }

    fun sendMessage() {
        val editingMessageId = uiState.value.editingMessageId
        if (editingMessageId != null) {
            saveEditedMessage(editingMessageId)
            return
        }

        val chatId = currentChatId ?: return
        val senderId = sessionRepository.currentUserId()
        val text = uiState.value.draftMessage.trim()

        // Защита от двойного нажатия
        if (_uiState.value.isSending) return

        if (senderId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Пользователь не авторизован") }
            return
        }

        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null, draftMessage = "") }

            try {
                val sent = repository.sendMessage(
                    chatId = chatId,
                    senderId = senderId,
                    text = text
                )

                // Добавляем отправленное сообщение сразу (Realtime может чуть опоздать)
                _uiState.update { state ->
                    val alreadyExists = state.messages.any { it.id == sent.id ||
                            (!sent.clientId.isNullOrBlank() && it.clientId == sent.clientId) }
                    val updatedMessages = if (alreadyExists) state.messages
                    else state.messages + sent
                    state.copy(
                        messages = updatedMessages,
                        isSending = false,
                        error = null,
                        editingMessageId = null,
                        isEditing = false
                    )
                }

                Log.d("ChatViewModel", "sendMessage success chatId=$chatId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed", e)
                // Возвращаем черновик обратно если отправка упала
                _uiState.update {
                    it.copy(
                        isSending = false,
                        draftMessage = text,
                        error = e.message ?: "Не удалось отправить сообщение"
                    )
                }
            }
        }
    }

    private fun saveEditedMessage(messageId: String) {
        val chatId = currentChatId ?: return
        val trimmed = uiState.value.draftMessage.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            try {
                repository.editMessage(messageId, trimmed)
                val updatedMessages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(
                        messages = updatedMessages,
                        draftMessage = "",
                        editingMessageId = null,
                        isEditing = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "editMessage failed messageId=$messageId", e)
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось отредактировать сообщение")
                }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId)
                val updatedMessages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(messages = updatedMessages, error = null)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "deleteMessage failed messageId=$messageId", e)
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось удалить сообщение")
                }
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        val chatId = currentChatId ?: return
        val trimmed = newText.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            try {
                repository.editMessage(messageId, trimmed)
                val updatedMessages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(messages = updatedMessages, error = null)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "editMessage failed messageId=$messageId", e)
                _uiState.update {
                    it.copy(error = e.message ?: "Не удалось отредактировать сообщение")
                }
            }
        }
    }
}
