package com.example.momenty.domain.home

import com.google.gson.annotations.SerializedName

data class LoadQuestData(
    @SerializedName(value="questId") val questId: Long,
    @SerializedName(value="date") val date: String,
    @SerializedName(value="quest") val quest: String
)