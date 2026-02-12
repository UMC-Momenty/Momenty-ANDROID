package com.example.momenty

import java.text.SimpleDateFormat
import java.util.*

object NotificationRepository {
    private val notificationList = mutableListOf<NotificationItem>()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    // 기록하지 않은 날짜 추적
    private val missedDates = mutableSetOf<String>()

    fun getAllNotifications(): List<NotificationItem> {
        return notificationList.toList()
    }

    // 날짜별 미기록 알림 추가
    fun addMissedDateNotification(date: String) {
        if (!missedDates.contains(date)) {
            missedDates.add(date)
            val notification = NotificationItem(
                id = notificationList.size + 1,
                message = "$date\n오늘의 모먼트를 아직 기록하지 않았어요!",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            notificationList.add(0, notification)
        }
    }

    // 일반 알림 추가
    fun addNotification(message: String) {
        val notification = NotificationItem(
            id = notificationList.size + 1,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        notificationList.add(0, notification)
    }

    // 날짜 기록 완료 시 해당 날짜 알림 제거
    fun removeMissedDateNotification(date: String) {
        missedDates.remove(date)
        notificationList.removeAll { it.message.contains(date) && it.message.contains("기록하지 않았어요") }
    }

    fun markAsRead(id: Int) {
        val index = notificationList.indexOfFirst { it.id == id }
        if (index != -1) {
            notificationList[index] = notificationList[index].copy(isRead = true)
        }
    }

    fun clearAll() {
        notificationList.clear()
        missedDates.clear()
    }
}