package com.roman.mars.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.roman.mars.data.repository.ChatRepository
import com.roman.mars.data.repository.MessageRepository

class MessageViewModelFactory(
    private val repository: MessageRepository,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            return MessageViewModel(repository, chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}