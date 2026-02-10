package com.example.momenty.domain.calendar

import java.util.Date

data class CalendarEvent(
    val id: String,
    val petId: String? = null, // 반려동물 ID (필터링용)
    val calendarId: String,
    val title: String,
    val ScheduleTime: Date, // 날짜
    val AlarmTime: String, // 시간
    val petName: String? = null, // 반려동물 이름
    val type: String // 일정 종류
)