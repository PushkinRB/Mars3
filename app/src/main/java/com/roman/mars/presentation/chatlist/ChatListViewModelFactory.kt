package com.roman.mars.presentation.chatlist
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roman.mars.data.repository.ChatRepository

class ChatListViewModelFactory(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            return ChatListViewModel(chatRepository, messageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}