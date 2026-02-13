package com.example.momenty.domain.home

import com.google.gson.annotations.SerializedName

data class WriteQuestResponse(
    @SerializedName(value="isSuccess") val isSuccess: Boolean,
    @SerializedName(value="code") val code: String,
    @SerializedName(value="message") val message: String,
    @SerializedName(value="result") val result: WriteQuestData?
)

data class WriteQuestData(
    @SerializedName(value="date") val date: String,
    @SerializedName(value="quest") val quest: String,
    @SerializedName(value="answer") val answer: String
)