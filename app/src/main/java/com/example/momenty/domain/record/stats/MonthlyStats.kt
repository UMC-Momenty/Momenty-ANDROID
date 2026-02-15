package com.example.momenty.domain.record.stats

data class MonthlyStats(
    val year: Int,
    val month: Int,
    val totalDays: Int,
    val recordedDays: Int,
    val moodGood: Int,
    val moodSoso: Int,
    val moodBad: Int
) {
    val recordRatePercent: Int
        get() = if (totalDays <= 0) 0 else (recordedDays * 100 / totalDays)

    val totalMoodCount: Int
        get() = (moodGood + moodSoso + moodBad).coerceAtLeast(1)

    val goodPercent: Int get() = moodGood * 100 / totalMoodCount
    val sosoPercent: Int get() = moodSoso * 100 / totalMoodCount
    val badPercent: Int get() = moodBad * 100 / totalMoodCount
}
