package com.example.momenty.domain.calendar.api.response

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
)