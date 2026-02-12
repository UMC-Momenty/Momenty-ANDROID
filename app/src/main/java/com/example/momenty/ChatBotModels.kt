package com.example.momenty

data class ChatRoomItem(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String,
    val messages: MutableList<ChatMessageItem> = mutableListOf()
)

data class ChatMessageItem(
    val id: String,
    val text: String,
    val isUser: Boolean
)