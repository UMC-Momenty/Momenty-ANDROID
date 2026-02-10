package com.example.momenty.domain.community

enum class CommunityCategory(val label: String) {
    ALL("전체"),
    QNA("질문"),
    INFO("정보공유"),
    REVIEW("후기"),
}


data class CommunityPostUiModel(
    val id: Long,
    val category: CommunityCategory,
    val author: String,
    val dateText: String,
    val title: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
)
