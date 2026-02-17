package com.example.momenty.data.repository

import com.example.momenty.data.remote.chat.dto.ChatResultDto

interface ChatRepository {
    suspend fun requestFirst(userId: Long, message: String): ChatResultDto
    suspend fun requestConversation(conversationId: Long, message: String): ChatResultDto
}
