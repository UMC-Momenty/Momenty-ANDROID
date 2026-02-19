package com.example.momenty.data.remote.chat

import com.example.momenty.data.remote.chat.dto.ChatRequestDto
import com.example.momenty.data.remote.chat.dto.ChatResultDto
import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {

    @POST("api/chat/users")
    suspend fun postFirstChat(
        @Body body: ChatRequestDto
    ): BaseResponse<ChatResultDto>

    @POST("api/chat/conversations/{conversationId}")
    suspend fun postConversationChat(
        @Path("conversationId") conversationId: Long,
        @Body body: ChatRequestDto
    ): BaseResponse<ChatResultDto>
}
