package com.roman.mars.data.model
data class Chat(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val isLastMessageMine: Boolean
)