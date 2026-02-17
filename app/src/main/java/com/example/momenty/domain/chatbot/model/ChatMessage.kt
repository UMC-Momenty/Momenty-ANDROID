package com.example.momenty.domain.chatbot.model

enum class Sender { USER, BOT }

data class ChatMessage(
    val id: Long,
    val sender: Sender,
    val text: String
)
