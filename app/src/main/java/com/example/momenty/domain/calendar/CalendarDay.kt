package com.example.momenty.domain.calendar

import java.time.Month
import java.util.Calendar

data class CalendarDay(
    val day: Int,
    val month: Int,
    val year: Int,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val events: List<CalendarEvent> = emptyList()
) {
    fun toCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun isSameDay(calendar: Calendar): Boolean {
        return year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) &&
                day == calendar.get(Calendar.DAY_OF_MONTH)
    }
}
