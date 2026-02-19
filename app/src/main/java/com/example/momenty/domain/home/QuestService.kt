package com.example.momenty.domain.home

import com.example.momenty.global.api.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface QuestService {
    @GET("api/quest/today")
    //fun loadQuest(@Header("Authorization") token: String): Call<LoadQuestResponse>
    suspend fun loadQuest(): Response<BaseResponse<LoadQuestData>>

    @POST("api/users/{userId}/answers")
    suspend fun writeQuest(
        @Path("userId") id: Long,
        @Body req: WriteQuestRequest
    ): Response<BaseResponse<String>>
    //fun writeQuest(@Body writeQuestRequest: WriteQuestRequest): Call<WriteQuestResponse>
}
