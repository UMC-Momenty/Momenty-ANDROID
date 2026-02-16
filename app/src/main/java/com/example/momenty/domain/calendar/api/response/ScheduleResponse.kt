package com.example.momenty.domain.calendar.api.response

import com.example.momenty.domain.calendar.CalendarEvent

/**
 * 모든 반려동물 월별 일정 조회 응답
 */
data class MonthlyScheduleResponse(
    val year: Int,
    val month: Int,
    val days: List<DayScheduleCountDto>
)

/**
 * 반려동물별 월별 일정 조회 응답
 */
data class PetMonthlyScheduleResponse(
    val petId: Long,
    val year: Int,
    val month: Int,
    val days: List<DayScheduleCountDto>
)

data class DayScheduleCountDto(
    val date: String,  // "YYYY-MM-DD"
    val count: Int
)

/**
 * 일별 일정 조회 응답
 */
data class DailyScheduleResponse(
    val petId: Long,
    val date: String,
    val schedules: List<ScheduleDto>
)

data class ScheduleDto(
    val scheduleId: Long,
    val title: String,
    val startAt: String,      // ISO 8601
    val memo: String?,
    val category: String
) {
    /**
     * DTO -> CalendarEvent 도메인 모델 변환
     */
    fun toDomain(petId: Long): CalendarEvent {
        return CalendarEvent(
            scheduleId = scheduleId,
            petId = petId,
            title = title,
            startAt = startAt,
            memo = memo,
            category = category,
            durationMinutes = null,
            isAlarmEnabled = true
        )
    }
}