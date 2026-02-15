package com.example.momenty.domain.record.stats

data class RecordCalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val hasRecord: Boolean
)
