package com.example.momenty.domain.record.stats

import java.util.Calendar

object RecordCalendarFactory {

    fun createMonthDays(
        year: Int,
        month0: Int,
        recordedDays: Set<Int>
    ): List<RecordCalendarDay> {

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month0)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val result = mutableListOf<RecordCalendarDay>()


        val leadingBlanks = firstDayOfWeek - Calendar.SUNDAY
        repeat(leadingBlanks) {
            result.add(RecordCalendarDay(day = 0, isCurrentMonth = false, hasRecord = false))
        }


        for (d in 1..daysInMonth) {
            result.add(
                RecordCalendarDay(
                    day = d,
                    isCurrentMonth = true,
                    hasRecord = recordedDays.contains(d)
                )
            )
        }

        // 마지막 줄 빈칸 채워서 7의 배수로 맞추기
        while (result.size % 7 != 0) {
            result.add(RecordCalendarDay(day = 0, isCurrentMonth = false, hasRecord = false))
        }

        return result
    }
}
