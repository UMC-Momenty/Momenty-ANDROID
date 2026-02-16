package com.example.momenty.domain.record.detail

import com.example.momenty.data.remote.moment.EmotionDto

data class MomentDetailUiModel(
    val imageUrls: List<String>,
    val dateText: String,
    val moodChipText: String,
    val moodTitleText: String,
    val titleText: String,
    val bodyText: String,
    val emotion: EmotionDto
)
