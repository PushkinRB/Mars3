package com.roman.mars.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.roman.mars.ui.theme.MarsColors

@Composable
fun MarsMessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    placeholder: String = "Сообщение",
isEditing: Boolean = false,
onCancelEditing: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isEditing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Редактирование сообщения",
                color = MarsColors.AccentSoft
                )
                if (onCancelEditing != null) {
                    TextButton(onClick = onCancelEditing) {
                        Text(
                            text = "Отмена",
                        color = MarsColors.TextSecondary
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MarsColors.TextMuted
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MarsColors.TextPrimary,
                    unfocusedTextColor = MarsColors.TextPrimary,
                    focusedBorderColor = MarsColors.Accent,
                    unfocusedBorderColor = MarsColors.TextMuted,
                    cursorColor = MarsColors.Accent,
                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                )
            )
            IconButton(
                onClick = onSendClick,
                modifier = Modifier.background(
                    color = MarsColors.Accent,
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = if (isEditing) "Сохранить" else "Отправить",
                    tint = Color.White
                )
            }
        }
    }
}