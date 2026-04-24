package com.roman.mars.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roman.mars.data.supabase.MessageDto
import com.roman.mars.data.supabase.MessagesRepository
import com.roman.mars.ui.auth.SessionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel( private val repository: MessagesRepository = MessagesRepository(), private val sessionRepository: SessionRepository = SessionRepository() ) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = emptyList<MessageDto>()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null
    private var pollingJob: Job? = null

    fun setChat(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        loadMessages()
        startPolling()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2500)
                loadMessagesSilently()
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    fun loadMessages() {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                val messages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(
                        messages = messages,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to load messages for chatId=$chatId", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Не удалось загрузить сообщения"
                    )
                }
            }
        }
    }

    private fun loadMessagesSilently() {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            try {
                val messages = repository.loadMessages(chatId)
                _uiState.update {
                    it.copy(
                        messages = messages,
                        error = null
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.update {
            it.copy(draftMessage = value)
        }
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

        if (_uiState.value.isSending) {
            return
        }

        Log.d("ChatViewModel", "sendMessage chatId=$chatId senderId=$senderId textLength=${text.length}")

        if (senderId.isNullOrBlank()) {
            _uiState.update {
                it.copy(error = "Пользователь не авторизован")
            }
            return
        }

        if (text.isBlank()) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSending = true, error = null)
            }

            try {
                repository.sendMessage(
                    chatId = chatId,
                    senderId = senderId,
                    text = text
                )

                val updatedMessages = repository.loadMessages(chatId)

                _uiState.update {
                    it.copy(
                        messages = updatedMessages,
                        isSending = false,
                        error = null,
                        draftMessage = "",
                        editingMessageId = null,
                        isEditing = false
                    )
                }

                Log.d("ChatViewModel", "sendMessage success for chatId=$chatId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed for chatId=$chatId senderId=$senderId", e)
                _uiState.update {
                    it.copy(
                        isSending = false,
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
                Log.e("ChatViewModel", "editMessage failed for messageId=$messageId", e)
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Не удалось отредактировать сообщение"
                    )
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
                    it.copy(
                        messages = updatedMessages,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "deleteMessage failed for messageId=$messageId", e)
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Не удалось удалить сообщение"
                    )
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
                    it.copy(
                        messages = updatedMessages,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "editMessage failed for messageId=$messageId", e)
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Не удалось отредактировать сообщение"
                    )
                }
            }
        }
    }
}