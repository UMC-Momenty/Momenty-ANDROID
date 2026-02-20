package com.example.momenty.domain.notification

data class NotificationUiModel(
    val id: Long,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean,
    val destination: NotificationDestination
)

enum class NotificationType {
    HOME, MYPAGE, RECORD, CHATBOT, SYSTEM
}

sealed class NotificationDestination {
    data class NavRes(val actionId: Int) : NotificationDestination()
    data class DeepLink(val uri: String) : NotificationDestination()
    data class Activity(val className: String) : NotificationDestination()
    data object None : NotificationDestination()
}