package com.example.momenty.domain.home

import com.google.gson.annotations.SerializedName

data class WriteQuestRequest(
    @SerializedName(value="questId") var questId: String,
    @SerializedName(value="petId") var petId: String,
    @SerializedName(value="answer") var answer: String
) {
    var id: Int = 0
}
