package com.example.momenty.domain.home

import com.google.gson.annotations.SerializedName

data class LoadQuestResponse<T>(
    @SerializedName(value="isSuccess") val isSuccess: Boolean,
    @SerializedName(value="code") val code: String,
    @SerializedName(value="message") val message: String,
    @SerializedName(value="result") val result: T? =null
)

data class LoadQuestData(
    @SerializedName(value="questId") val questId: String = "1",
    @SerializedName(value="date") val date: String,
    @SerializedName(value="quest") val quest: String
)