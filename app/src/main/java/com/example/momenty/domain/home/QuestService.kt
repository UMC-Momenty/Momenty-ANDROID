package com.example.momenty.domain.home

import com.example.momenty.global.api.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface QuestService {
    @GET("api/quest/today")
    //fun loadQuest(@Header("Authorization") token: String): Call<LoadQuestResponse>
    suspend fun loadQuest(
        @Header("Authorization") token: String
    ): Response<BaseResponse<LoadQuestData>>

    @POST("api/quest")
    suspend fun writeQuest(
        @Header("Authorization") token: String,
        @Body req: WriteQuestRequest
    ): Response<BaseResponse<WriteQuestData>>
    //fun writeQuest(@Body writeQuestRequest: WriteQuestRequest): Call<WriteQuestResponse>
}
