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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: MessagesRepository = MessagesRepository(),
    private val sessionRepository: SessionRepository = SessionRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = emptyList<MessageDto>()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null
    // УДАЛЕНО: private var pollingJob: Job? = null
    // НОВOE: Job для нашей новой подписки на Realtime
    private var realtimeJob: Job? = null

    fun setChat(chatId: String) {
        if (currentChatId == chatId) return
        currentChatId = chatId
        // Останавливаем прослушивание старого чата, если он был
        realtimeJob?.cancel()
        loadMessages()
        // Запускаем прослушивание нового чата
        listenForNewMessages()
    }

    // УДАЛЕНО: Вся функция startPolling() и loadMessagesSilently() больше не нужны.

    override fun onCleared() {
        // Убеждаемся, что отписались от прослушивания, когда экран закрывается
        realtimeJob?.cancel()
        super.onCleared()
    }

    // НОВАЯ ФУНКЦИЯ: Запускает прослушивание сообщений в реальном времени
    private fun listenForNewMessages() {
        val chatId = currentChatId ?: return
        // Отменяем предыдущую подписку, если она была
        realtimeJob?.cancel()

        realtimeJob = viewModelScope.launch {
            repository.listenForNewMessages(chatId)
                .onEach { newMessage ->
                    // Когда приходит новое сообщение, мы добавляем его в конец списка
                    Log.d("ChatViewModel", "New message received via Realtime: ${newMessage.text}")
                    _uiState.update { currentState ->
                        currentState.copy(messages = currentState.messages + newMessage)
                    }
                }
                .catch { e ->
                    Log.e("ChatViewModel", "Realtime error for chatId=$chatId", e)
                }
                .launchIn(viewModelScope)
        }
    }


    fun loadMessages() {
        val chatId = currentChatId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                // При первой загрузке мы по-прежнему получаем всю историю сообщений
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

        // РЕШЕНИЕ ПРОБЛЕМЫ №2: Если сообщение уже отправляется, выходим.
        // Это предотвращает многократные нажатия и дубли.
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
            // 1. Устанавливаем флаг "отправляется"
            _uiState.update {
                it.copy(isSending = true, error = null, draftMessage = "")
            }

            try {
                // 2. Отправляем сообщение
                repository.sendMessage(
                    chatId = chatId,
                    senderId = senderId,
                    text = text
                )
                // 3. УСПЕХ! Снимаем флаг "отправляется".
                // ВАЖНО: Мы больше не перезагружаем сообщения здесь!
                // Наше отправленное сообщение придет через Realtime-подписку,
                // что гарантирует плавность и отсутствие дублей.
                _uiState.update {
                    it.copy(isSending = false)
                }

                Log.d("ChatViewModel", "sendMessage success for chatId=$chatId")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "sendMessage failed for chatId=$chatId senderId=$senderId", e)
                // 4. ОШИБКА! Снимаем флаг "отправляется" и возвращаем текст, чтобы пользователь мог попробовать снова.
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Не удалось отправить сообщение",
                        draftMessage = text // Возвращаем текст в поле ввода
                    )
                }
            }
        }
    }

    private fun saveEditedMessage(messageId: String) {
        // Логика редактирования остается прежней, но после успешного
        // редактирования, обновление придет через Realtime.
        // Для простоты пока оставим перезагрузку.
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
        // Логику удаления пока оставляем прежней
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
}

