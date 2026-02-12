package com.example.momenty.domain.calendar

import java.util.Date

data class Alarm(
    val id: Long = System.currentTimeMillis(),
    val activityType: String, // 활동 유형 (산책, 식사, 미용 등)
    val title: String, // 일정 이름
    val petId: String, // 반려동물 필터
    val isRepeat: Boolean, // true: 반복성, false: 일회성
    val repeatDays: List<Int>? = null, // 반복 요일 (1=월, 2=화, ..., 7=일) - 반복성일 때만 사용
    val alarmDate: Date, // 알림 날짜 (일회성일 때 사용, 반복성일 때는 시작 날짜)
    val alarmTime: String, // 알림 시간 (예: "오후 2:00")
    val duration: String?, // 지속 시간 (예: "1시간")
    val note: String? = null, // 메모 (선택 사항)
    var isEnabled: Boolean = true // 알림 활성화 여부
)