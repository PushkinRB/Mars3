package com.roman.mars.ui.chatlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.roman.mars.data.model.Chat

@Composable
fun SupabaseChatListRoute(
    viewModel: SupabaseChatListViewModel,
    onLogoutClick: () -> Unit,
    onChatClick: (Chat) -> Unit,
    onOpenContactsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }
    ChatListScreen(
        chats = uiState.chats,
        onChatClick = onChatClick,
        onOpenContacts = onOpenContactsClick,
        onDeleteChat = { chat ->
            viewModel.deleteChat(chat)
        },
        onLogoutClick = onLogoutClick
    )
}