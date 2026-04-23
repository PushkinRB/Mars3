package com.roman.mars.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.roman.mars.R
import com.roman.mars.data.model.Chat
import com.roman.mars.data.supabase.toUiMessage
import com.roman.mars.ui.auth.SessionRepository
import com.roman.mars.ui.theme.MarsColors

@Composable
fun SupabaseChatRoute(
    viewModel: ChatViewModel,
    chat: Chat,
    onBackClick: () -> Unit,
    onAddContactClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chat.id) {
        viewModel.setChat(chat.id)
    }
    val currentSenderId = SessionRepository().currentUserId().orEmpty()
    val uiMessages = uiState.messages.map { dto ->
        dto.toUiMessage(currentSenderId = currentSenderId)
    }
    val shouldShowEmptyPlaceholder =
        uiMessages.isEmpty() && chat.name.startsWith("Новый чат")
    if (shouldShowEmptyPlaceholder) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.mars_bg),
                contentDescription = "Фон чата",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MarsColors.OverlaySoft,
                                MarsColors.OverlayStrong
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
            ) {
                MarsChatTopBar(
                    title = chat.name,
                    onBackClick = onBackClick
                )
                EmptyChatPlaceholder(
                    onAddContactClick = onAddContactClick
                )
            }
        }
    } else {
        MarsChatScreen(
            title = chat.name,
            messages = uiMessages.map { message ->
                MarsUiMessage(
                    id = message.id,
                    text = message.text,
                    time = message.time,
                    isMine = message.isMine,
                    showChecks = message.isMine,
                    isEdited = false
                )
            },
            inputText = uiState.draftMessage,
            onInputChange = viewModel::onDraftChanged,
            onSendClick = {
                viewModel.sendMessage()
            },
            onBackClick = onBackClick,
            onEditMessage = { messageId, oldText ->
                viewModel.startEditing(messageId, oldText)
            },
            onDeleteMessage = { messageId ->
                viewModel.deleteMessage(messageId)
            },
            isEditing = uiState.isEditing,
            onCancelEditing = {
                viewModel.cancelEditing()
            }
        )
    }
}