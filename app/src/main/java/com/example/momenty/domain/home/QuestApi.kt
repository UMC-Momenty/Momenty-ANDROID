package com.example.momenty.domain.home

import com.example.momenty.global.api.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface QuestApi {
    @GET("api/quest/today")
    fun loadQuest(@Header("Authorization") token: String): Call<LoadQuestResponse>

    @POST("api/quest")
    fun writeQuest(@Body writeQuestRequest: WriteQuestRequest): Call<WriteQuestResponse>
}