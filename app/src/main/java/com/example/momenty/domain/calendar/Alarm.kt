package com.example.momenty.domain.calendar

data class Alarm(
    val scheduleId: Long,                    // id → scheduleId
    val title: String,
    val category: String,                    // activityType → category
    val petId: Long,                         // String → Long
    val isOneTime: Boolean,                  // isRepeat 반대 개념
    val repeatDays: List<String>? = null,    // List<Int> → List<String>, ["MONDAY", "WEDNESDAY"]
    val date: String? = null,                // 일회성인 경우 "YYYY-MM-DD"
    val alarmTime: String,                   // "HH:mm:ss" 형식
    val durationMinutes: Int,                // String → Int
    val memo: String? = null,                // note → memo
    var isAlarmEnabled: Boolean = true       // isEnabled → isAlarmEnabled
) {
    /**
     * 반복 요일을 한글로 표시
     */
    fun getRepeatDaysDisplay(): String {
        if (isOneTime || repeatDays.isNullOrEmpty()) {
            return date ?: ""
        }

        // 모든 요일 선택된 경우
        if (repeatDays.size == 7) {
            return "매일"
        }

        val dayMap = mapOf(
            "MONDAY" to "월",
            "TUESDAY" to "화",
            "WEDNESDAY" to "수",
            "THURSDAY" to "목",
            "FRIDAY" to "금",
            "SATURDAY" to "토",
            "SUNDAY" to "일"
        )

        return repeatDays.mapNotNull { day ->
            dayMap[day]
        }.joinToString(" ")
    }

    /**
     * 알림 시간을 "오전/오후 HH:mm" 형식으로 변환
     */
    fun getFormattedTime(): String {
        return try {
            val parts = alarmTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1]

            val period = if (hour < 12) "오전" else "오후"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }

            "$period $displayHour:$minute"
        } catch (e: Exception) {
            alarmTime
        }
    }
}
