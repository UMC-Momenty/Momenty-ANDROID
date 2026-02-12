package com.example.momenty

import java.text.SimpleDateFormat
import java.util.*

object MomentRepository {
    private var currentMoment: MomentItem? = null
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    var momentCount: Int = 20
        private set

    fun getTodayMoment(): MomentItem {
        if (currentMoment == null) {
            currentMoment = MomentItem("1", dateFormat.format(Date()), "", "", MomentState.EMPTY)
        }
        return currentMoment!!
    }

    fun updateMomentState(newState: MomentState) {
        currentMoment = currentMoment?.copy(state = newState)
        if (newState == MomentState.ALL_COMPLETE) {
            momentCount++
        }
    }

    // [필수 추가] MainActivity의 빨간 줄을 없애기 위한 함수들
    fun updateQuestion(question: String) {
        currentMoment = currentMoment?.copy(question = question)
    }

    fun updateAnswer(answer: String) {
        currentMoment = currentMoment?.copy(answer = answer)
    }

    fun getCurrentDate(): String = dateFormat.format(Date())
}