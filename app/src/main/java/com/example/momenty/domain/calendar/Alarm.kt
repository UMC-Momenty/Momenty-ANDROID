package com.example.momenty.domain.calendar

import java.util.Date

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
)