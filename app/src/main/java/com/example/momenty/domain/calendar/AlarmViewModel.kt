package com.example.momenty.domain.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlarmViewModel : ViewModel() {

    private val _alarms = MutableLiveData<List<Alarm>>()
    val alarms: LiveData<List<Alarm>> = _alarms

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
    * 알람 목록 로드
    */
    fun loadAlarms() {
        // TODO: 실제로는 서버나 로컬 DB에서 데이터를 가져옴
        // 현재는 더미 데이터로 테스트
        _alarms.value = getDummyAlarms()
    }

    /**
     * 알람 토글
     */
    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        val currentList = _alarms.value ?: emptyList()
        val updatedList = currentList.map {
            if (it.id == alarm.id) {
                it.copy(isEnabled = isEnabled)
            } else {
                it
            }
        }
        _alarms.value = updatedList

        // TODO: 서버나 로컬 DB에 상태 저장
    }

    /**
     * 알람 추가
     */
    fun addAlarm(alarm: Alarm) {
        val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
        currentList.add(alarm)
        _alarms.value = currentList

        // TODO: 서버나 로컬 DB에 저장
    }

    /**
     * 알람 삭제
     */
    fun deleteAlarm(alarm: Alarm) {
        val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
        currentList.remove(alarm)
        _alarms.value = currentList

        // TODO: 서버나 로컬 DB에서 삭제
    }

    /**
     * 더미 데이터 생성 (테스트용)
     */
    private fun getDummyAlarms(): List<Alarm> {
        return listOf(
            Alarm(
                id = 1,
                title = "산책",
                duration = "1시간",
                repeatDays = "월 화 수",
                time = "오후 07:00",
                isEnabled = true
            ),
            Alarm(
                id = 2,
                title = "약 주기",
                duration = "매일",
                repeatDays = "매일",
                time = "오전 06:00",
                isEnabled = true
            )
        )
    }
}