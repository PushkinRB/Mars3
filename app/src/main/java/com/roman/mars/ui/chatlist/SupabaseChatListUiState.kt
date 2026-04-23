package com.roman.mars.ui.chatlist

import com.roman.mars.data.model.Chat

data class SupabaseChatListUiState(
    val chats: kotlin.collections.List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)