package com.example.momenty.domain.community.detail


data class CommunityDetailPostUiModel(
    val categoryText: String,
    val author: String,
    val dateText: String,
    val title: String,
    val content: String,
    val likeCount: Int
)
