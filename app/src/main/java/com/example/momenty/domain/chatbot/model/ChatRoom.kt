package com.example.momenty.domain.chatbot.model

data class ChatRoom(
    val id: Long,
    val title: String,
    val lastPreview: String = "",
    val timeText: String = ""
)
