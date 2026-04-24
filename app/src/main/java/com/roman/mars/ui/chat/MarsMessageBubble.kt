package com.roman.mars.ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roman.mars.ui.theme.MarsColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarsMessageBubble(
    message: MarsUiMessage,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            if (message.isMine) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(
                        color = if (message.isMine) {
                            MarsColors.AccentDeep.copy(alpha = 0.88f)
                        } else {
                            Color.White.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isMine) 20.dp else 6.dp,
                            bottomEnd = if (message.isMine) 6.dp else 20.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            if (message.isMine) {
                                menuExpanded = true
                            }
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = MarsColors.TextPrimary,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.size(6.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "изменено",
                            color = MarsColors.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = message.time,
                        color = MarsColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (message.isMine && message.showChecks) {
                        Text(
                            text = "✓✓",
                            color = MarsColors.OnlineBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (!message.isMine) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = {
                    menuExpanded = false
                    onEditMessage(message.id, message.text)
                }
            )

            DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = {
                    menuExpanded = false
                    onDeleteMessage(message.id)
                }
            )
        }
    }
}