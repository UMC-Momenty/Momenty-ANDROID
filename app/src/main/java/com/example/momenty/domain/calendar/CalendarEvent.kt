package com.example.momenty.domain.calendar

import java.util.Date

data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Date,
    val endTime: Date,
    val description: String? = null,
    val calendarId: String,
    val petId: String? = null, // 반려동물 ID (필터링용)
    val petName: String? = null, // 반려동물 이름
    val color: Int? = null // 일정 색상
)