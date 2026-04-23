package com.roman.mars.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.roman.mars.R
import com.roman.mars.ui.theme.MarsColors

@Composable
fun MarsChatScreen(
    title: String,
    messages: List<MarsUiMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    isEditing: Boolean = false,
    onCancelEditing: (() -> Unit)? = null,
    bottomSpacer: Int = 12
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mars_bg),
            contentDescription = "Фон чата Mars",
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
        ) {
            MarsChatTopBar(
                title = title,
                onBackClick = onBackClick
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(messages) { message ->
                    MarsMessageBubble(
                        message = message,
                        onEditMessage = onEditMessage,
                        onDeleteMessage = onDeleteMessage
                    )
                }
                item { Spacer(modifier = Modifier.height(bottomSpacer.dp)) }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                MarsMessageInput(
                    value = inputText,
                    onValueChange = onInputChange,
                    onSendClick = onSendClick,
                    isEditing = isEditing,
                    onCancelEditing = onCancelEditing
                )
            }
        }
    }
}

data class MarsUiMessage(
    val id: String,
    val text: String,
    val time: String,
    val isMine: Boolean,
    val showChecks: Boolean = false,
    val isEdited: Boolean = false
)