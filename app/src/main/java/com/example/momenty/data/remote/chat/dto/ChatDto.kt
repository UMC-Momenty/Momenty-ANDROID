package com.example.momenty.data.remote.chat.dto

data class ChatRequestDto(
    val message: String
)

data class ChatResultDto(
    val chatId: Long,
    val role: String,
    val answer: String,
    val questionType: String
)
