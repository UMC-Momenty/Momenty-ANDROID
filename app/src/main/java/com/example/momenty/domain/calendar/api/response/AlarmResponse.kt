package com.example.momenty.domain.calendar.api.response

import com.example.momenty.domain.calendar.Alarm

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
) {
    /**
     * DTO를 Alarm 도메인 모델로 변환
     *
     * @param petId 반려동물 ID (response의 result.petId 사용)
     */
    fun toDomain(petId: Long): Alarm {
        return Alarm(
            scheduleId = scheduleId,
            title = title,
            category = category,
            petId = petId,
            isOneTime = isOneTime,
            repeatDays = repeatDays,
            date = date,
            alarmTime = alarmTime,
            durationMinutes = durationMinutes,
            memo = null,  // API 응답에 memo 없음
            isAlarmEnabled = isAlarmEnabled
        )
    }
}
