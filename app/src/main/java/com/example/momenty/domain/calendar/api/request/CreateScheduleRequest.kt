package com.example.momenty.domain.calendar.api.request

data class CreateScheduleRequest(
    val title: String,                      // [필수]
    val category: ScheduleCategory,         // [필수]
    val memo: String? = null,               // [선택]
    val time: String,                       // [필수] "HH:mm"
    val type: ScheduleType,                 // [필수] "ONE_TIME" or "REPEAT"

    // ONE_TIME인 경우 필수
    val date: String? = null,               // "YYYY-MM-DD"

    // REPEAT인 경우 필수
    val repeatDays: List<DayOfWeek>? = null,  // ["MONDAY", "WEDNESDAY"]

    val durationMinutes: Int? = null,       // [선택]
    val isAlarmEnabled: Boolean = true      // [선택] 기본값 true
)

enum class ScheduleCategory {
    WALK, MEAL, HEALTH, BEAUTY, MEDICINE, SNACK, ETC
}

enum class ScheduleType {
    ONE_TIME, REPEAT
}

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}