package com.example.momenty

// 1. 모먼트 관련 데이터 클래스 및 상태
data class MomentItem(
    val id: String,
    val date: String,
    val question: String,
    val answer: String,
    val state: MomentState
)

enum class MomentState {
    EMPTY, QUESTION_WRITING, QUESTION_COMPLETE, ANSWER_WRITING, ALL_COMPLETE
}

// 2. 알림 관련 데이터 클래스
data class NotificationItem(
    val id: Int,
    val message: String,
    val timestamp: Long,
    var isRead: Boolean = false
)