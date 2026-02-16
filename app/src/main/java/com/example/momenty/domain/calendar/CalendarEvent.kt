package com.example.momenty.domain.calendar

import java.util.Date

data class CalendarEvent(
    val scheduleId: Long,          // String → Long
    val petId: Long,               // String → Long, nullable 제거
    val title: String,
    val startAt: String,           // ISO 8601 형식 "YYYY-MM-DDTHH:mm:ss"
    val memo: String? = null,
    val category: String,          // "병원", "건강" 등
    val durationMinutes: Int?,
    val isAlarmEnabled: Boolean
)