package com.roman.mars.ui.chat

import com.roman.mars.data.supabase.MessageDto

data class ChatUiState(
    val messages: List<MessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val draftMessage: String = "",
    val editingMessageId: String? = null,
    val isEditing: Boolean = false
)
