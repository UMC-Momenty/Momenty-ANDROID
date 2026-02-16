package com.example.momenty.domain.calendar.api.response

/**
 * 일정 생성 API 응답
 */
data class CreateScheduleResponse(
    val scheduleId: String,
    val title: String
)