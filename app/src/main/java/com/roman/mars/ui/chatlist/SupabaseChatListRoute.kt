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
    onOpenContactsClick: () -> Unit,
    contactNames: Map<String, String> = emptyMap()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChats()
    }

    val chatsWithContactNames = uiState.chats.map { chat ->
        val contactName = resolveContactName(chat.otherUserPhone, contactNames)
        if (!contactName.isNullOrBlank()) chat.copy(name = contactName) else chat
    }

    ChatListScreen(
        chats = chatsWithContactNames,
        onChatClick = { chat -> onChatClick(chat) },
        onOpenContacts = onOpenContactsClick,
        onDeleteChat = { chat -> viewModel.deleteChat(chat) },
        onLogoutClick = onLogoutClick
    )
}

private fun resolveContactName(
    rawPhone: String?,
    contactNames: Map<String, String>
): String? {
    if (rawPhone.isNullOrBlank()) return null
    val normalized = rawPhone.replace(Regex("[^0-9]"), "").takeLast(10)
    if (normalized.length < 7) return null
    return contactNames.entries.firstOrNull { (phone, _) ->
        phone.replace(Regex("[^0-9]"), "").takeLast(10) == normalized
    }?.value
}
