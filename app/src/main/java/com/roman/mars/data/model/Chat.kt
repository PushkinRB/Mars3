package com.roman.mars.data.model

data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isLastMessageMine: Boolean = false,
    val otherUserPhone: String? = null
)
