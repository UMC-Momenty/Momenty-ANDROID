package com.example.momenty.domain.calendar

data class Alarm(
    val id: Long = 0,
    val title: String,
    val duration: String,
    val repeatDays: String,
    val time: String,
    val isEnabled: Boolean = true
)