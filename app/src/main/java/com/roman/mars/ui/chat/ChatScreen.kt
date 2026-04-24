package com.roman.mars.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roman.mars.R
import com.roman.mars.data.model.Chat
import com.roman.mars.data.model.Message

@Composable
fun ChatScreen(
    chat: Chat,
    messages: List<Message>,
    isSending: Boolean = false,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val editingMessageState = remember { mutableStateOf<Message?>(null) }
    val editingMessage = editingMessageState.value
    val lastMineMessageId = messages.lastOrNull { it.isMine }?.id

    // Автоскролл вниз при новых сообщениях
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

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
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
        ) {
            ChatTopBar(
                chatName = chat.name,
                onBackClick = onBackClick
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(messages) { _, message ->
                    MessageBubble(
                        message = message,
                        isRead = message.isMine && message.id == lastMineMessageId,
                        onEditClick = {
                            editingMessageState.value = message
                        },
                        onDeleteClick = {
                            onDeleteMessage(message.id)
                        }
                    )
                }
            }
            MessageInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                isSending = isSending,
                onSendClick = {
                    val trimmed = inputText.trim()
                    if (trimmed.isNotEmpty() && !isSending) {
                        onSendMessage(trimmed)
                        inputText = ""
                    }
                }
            )
        }
        if (editingMessage != null) {
            EditMessageDialog(
                initialText = editingMessage.text,
                onDismiss = { editingMessageState.value = null },
                onConfirm = { newText ->
                    onEditMessage(editingMessage.id, newText)
                    editingMessageState.value = null
                }
            )
        }
    }
}

@Composable
fun ChatTopBar(
    chatName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xCC3A3A3A)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Назад")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = chatName,
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isRead: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Card(
                modifier = if (message.isMine) {
                    Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = { menuExpanded = true }
                    )
                } else {
                    Modifier
                },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isMine)
                        Color(0xFFFF6F00)
                    else
                        Color.White.copy(alpha = 0.16f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.time,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                        if (message.isMine) {
                            Spacer(modifier = Modifier.width(4.dp))
                            // Одна галочка = отправлено, две = доставлено
                            Text(
                                text = "✓✓",
                                color = if (isRead) Color(0xFF4FC3F7)
                                else Color(0xFFD6D6D6),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            if (message.isMine) {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Редактировать") },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить") },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditMessageDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var editedText by remember(initialText) { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать сообщение") },
        text = {
            OutlinedTextField(
                value = editedText,
                onValueChange = { editedText = it },
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmed = editedText.trim()
                if (trimmed.isNotEmpty()) onConfirm(trimmed)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = !isSending,
            placeholder = { Text("Сообщение") },
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.12f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.65f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.65f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.07f),
                disabledTextColor = Color.White.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            enabled = !isSending,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6F00),
                disabledContainerColor = Color(0xFF7A3800)
            )
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .width(18.dp)
                        .height(18.dp)
                )
            } else {
                Text("Отпр.")
            }
        }
    }
}
