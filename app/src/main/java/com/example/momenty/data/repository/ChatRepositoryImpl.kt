package com.example.momenty.data.repository

import com.example.momenty.data.remote.chat.ChatApi
import com.example.momenty.data.remote.chat.dto.ChatRequestDto
import com.example.momenty.data.remote.chat.dto.ChatResultDto
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApi: ChatApi
) : ChatRepository {

    override suspend fun requestFirst(userId: Long, message: String): ChatResultDto {
        val res = chatApi.postFirstChat(userId, ChatRequestDto(message))
        if (!res.isSuccess || res.result == null) {
            throw IllegalStateException("Chat first failed: ${res.message}")
        }
        return res.result
    }

    override suspend fun requestConversation(conversationId: Long, message: String): ChatResultDto {
        val res = chatApi.postConversationChat(conversationId, ChatRequestDto(message))
        if (!res.isSuccess || res.result == null) {
            throw IllegalStateException("Chat conversation failed: ${res.message}")
        }
        return res.result
    }
}
