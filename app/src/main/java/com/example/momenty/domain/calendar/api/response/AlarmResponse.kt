package com.example.momenty.domain.calendar.api.response

/**
 * 알림 목록 조회 응답
 */
data class AlarmListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: AlarmListResult
)

data class AlarmListResult(
    val petId: Long,
    val alarms: List<AlarmDto>
)

data class AlarmDto(
    val scheduleId: Long,
    val title: String,
    val category: String,
    val repeatDays: List<String>? = null,
    val date: String? = null,
    val alarmTime: String,
    val durationMinutes: Int,
    val isOneTime: Boolean,
    val isAlarmEnabled: Boolean
)