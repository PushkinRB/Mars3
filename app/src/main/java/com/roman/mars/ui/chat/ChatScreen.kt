// ... импорты остаются прежними ...
import com.roman.mars.data.model.Chat
import com.roman.mars.data.model.Message

// ДОБАВЛЯЕМ новый параметр isSending
@Composable
fun ChatScreen(
    chat: Chat,
    messages: List<Message>,
    isSending: Boolean, // <<< НОВЫЙ ПАРАМЕТР
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEditMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit
) {
    // ... остальной код функции остается без изменений ...
    // ...
    LazyColumn(
        // ...
    ) {
        // ...
    }
    MessageInputBar(
        value = inputText,
        onValueChange = { inputText = it },
        onSendClick = {
            val trimmed = inputText.trim()
            if (trimmed.isNotEmpty()) {
                onSendMessage(trimmed)
                inputText = ""
            }
        },
        // ПЕРЕДАЕМ isSending в панель ввода
        isSending = isSending // <<< НОВОЕ
    )
}
if (editingMessage != null) {
    // ...
}
}
}
// ... ChatTopBar и MessageBubble остаются без изменений ...

// ... EditMessageDialog остается без изменений ...

// ДОБАВЛЯЕМ новый параметр isSending
@Composable
fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isSending: Boolean // <<< НОВЫЙ ПАРАМЕТР
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            // ...
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            // Используем isSending, чтобы блокировать кнопку
            enabled = !isSending, // <<< НОВОЕ
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF6F00)
            )
        ) {
            // Можно добавить индикатор для наглядности
            Text(if (isSending) "..." else "Отпр.") // <<< НОВОЕ
        }
    }
}

