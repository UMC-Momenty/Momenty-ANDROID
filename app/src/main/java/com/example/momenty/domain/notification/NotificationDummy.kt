package com.example.momenty.domain.notification

import com.example.momenty.R

object NotificationDummy {

    fun initial(): List<NotificationUiModel> = listOf(
        NotificationUiModel(
            id = 1,
            message = "오늘의 모먼트를 이미 기록하셨네요!",
            timeAgo = "1분 전",
            type = NotificationType.RECORD,
            isRead = false,
            destination = NotificationDestination.DeepLink("momenty://record/write")
        ),
        NotificationUiModel(
            id = 2,
            message = "오늘의 질문을 확인해보세요!",
            timeAgo = "3분 전",
            type = NotificationType.HOME,
            isRead = false,
            destination = NotificationDestination.Activity("com.example.momenty.domain.home.QuestActivity")
        ),
        NotificationUiModel(
            id = 3,
            message = "마이페이지를 업데이트 해보세요!",
            timeAgo = "12분 전",
            type = NotificationType.MYPAGE,
            isRead = false,
            destination = NotificationDestination.NavRes(R.id.action_global_to_record)
        ),
        NotificationUiModel(
            id = 4,
            message = "새로운 기능이 업데이트 되었어요!",
            timeAgo = "2시간 전",
            type = NotificationType.SYSTEM,
            isRead = true,
            destination = NotificationDestination.None
        ),
        NotificationUiModel(
            id = 5,
            message = "챗봇이 기다리고 있어요 :)",
            timeAgo = "1일 전",
            type = NotificationType.CHATBOT,
            isRead = true,
            destination = NotificationDestination.NavRes(R.id.action_global_to_chatbot)
        )
    )
}