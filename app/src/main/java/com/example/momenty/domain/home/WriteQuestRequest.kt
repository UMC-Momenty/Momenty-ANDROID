package com.example.momenty.domain.home

import com.google.gson.annotations.SerializedName

data class WriteQuestRequest(
    @SerializedName(value="questId") var questId: Long,
    @SerializedName(value="petId") var petId: Long,
    @SerializedName(value="answer") var answer: String
) {
    var id: Int = 0
}
