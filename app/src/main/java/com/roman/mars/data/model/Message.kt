package com.roman.mars.data.model

data class Message(
    val id: String,
    val chatId: String,
    val text: String,
    val time: String,
    val isMine: Boolean
)