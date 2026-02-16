package com.example.momenty.domain.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.momenty.global.security.LocalDataManager
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val localDataManager = LocalDataManager(application.applicationContext)

    private val _alarms = MutableLiveData<List<Alarm>>()
    val alarms: LiveData<List<Alarm>> = _alarms

    private val _filteredAlarms = MutableLiveData<List<Alarm>>()
    val filteredAlarms: LiveData<List<Alarm>> = _filteredAlarms

    private val _selectedPet = MutableLiveData<Pet?>()
    val selectedPet: LiveData<Pet?> = _selectedPet

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _navigateToAlarmList = MutableLiveData<Boolean>()
    val navigateToAlarmList: LiveData<Boolean> = _navigateToAlarmList

    init {
        _alarms.value = emptyList()
        _filteredAlarms.value = emptyList()
        _selectedPet.value = null
    }

    /**
     * 알람 목록 로드 - LocalDataManager에서 가져오기
     */
    fun loadAlarms() {
        viewModelScope.launch {
            try {
                // LocalDataManager에서 알림이 활성화된 일정 가져오기
                val schedules = localDataManager.getAlarmsEnabled()

                android.util.Log.d("AlarmViewModel", "Loaded ${schedules.size} alarms from local storage")

                // LocalSchedule을 Alarm으로 변환
                val alarmList = schedules.map { schedule ->
                    Alarm(
                        scheduleId = schedule.scheduleId,
                        title = schedule.title,
                        category = schedule.category,
                        petId = schedule.petId,
                        isOneTime = schedule.isOneTime,
                        repeatDays = schedule.repeatDays,
                        date = schedule.date,
                        alarmTime = schedule.time + ":00", // "HH:mm" -> "HH:mm:ss"
                        durationMinutes = schedule.durationMinutes ?: 0,
                        isAlarmEnabled = schedule.isAlarmEnabled
                    )
                }

                _alarms.value = alarmList
                applyPetFilter()

                if (alarmList.isEmpty()) {
                    android.util.Log.d("AlarmViewModel", "No alarms found")
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmViewModel", "Error loading alarms", e)
                _error.value = "알람 로드 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 반려동물 필터 선택
     */
    fun selectPetFilter(pet: Pet?) {
        _selectedPet.value = pet
        applyPetFilter()
    }

    /**
     * 반려동물별 필터링 적용
     */
    private fun applyPetFilter() {
        val currentAlarms = _alarms.value ?: emptyList()
        val selectedPetId = _selectedPet.value?.petId

        _filteredAlarms.value = if (selectedPetId == null) {
            // 전체 알람 표시
            currentAlarms
        } else {
            // 선택된 반려동물의 알람만 표시
            currentAlarms.filter { it.petId == selectedPetId }
        }
    }

    /**
     * 알람 토글 - LocalDataManager에 저장
     */
    fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        viewModelScope.launch {
            try {
                // LocalDataManager에 상태 저장
                localDataManager.toggleAlarmStatus(alarm.scheduleId, isEnabled)

                android.util.Log.d("AlarmViewModel", "Alarm ${alarm.scheduleId} toggled to $isEnabled")

                // UI 업데이트
                val currentList = _alarms.value ?: emptyList()
                val updatedList = currentList.map {
                    if (it.scheduleId == alarm.scheduleId) {
                        it.copy(isAlarmEnabled = isEnabled)
                    } else {
                        it
                    }
                }
                _alarms.value = updatedList
                applyPetFilter()
            } catch (e: Exception) {
                android.util.Log.e("AlarmViewModel", "Error toggling alarm", e)
                _error.value = "알람 상태 변경 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 알람 추가 - LocalDataManager에 저장
     */
    fun addAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                // Alarm을 LocalSchedule로 변환
                val schedule = com.example.momenty.global.mock.LocalSchedule(
                    scheduleId = alarm.scheduleId,
                    petId = alarm.petId,
                    title = alarm.title,
                    category = alarm.category,
                    date = alarm.date,
                    repeatDays = alarm.repeatDays,
                    time = alarm.alarmTime.substring(0, 5), // "HH:mm:ss" -> "HH:mm"
                    durationMinutes = alarm.durationMinutes,
                    memo = null,
                    isAlarmEnabled = alarm.isAlarmEnabled,
                    isOneTime = alarm.isOneTime
                )

                // LocalDataManager에 저장
                localDataManager.addSchedule(schedule)

                android.util.Log.d("AlarmViewModel", "Alarm added: ${alarm.title}")

                // UI 업데이트
                val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
                currentList.add(alarm)
                _alarms.value = currentList
                applyPetFilter()

                // 알람 리스트로 이동 트리거
                _navigateToAlarmList.value = true
            } catch (e: Exception) {
                android.util.Log.e("AlarmViewModel", "Error adding alarm", e)
                _error.value = "알람 추가 중 오류가 발생했습니다"
            }
        }
    }

    /**
     * 알람 삭제 - LocalDataManager에서 삭제
     */
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                // LocalDataManager에서 삭제
                localDataManager.deleteSchedule(alarm.scheduleId)

                android.util.Log.d("AlarmViewModel", "Alarm deleted: ${alarm.scheduleId}")

                // UI 업데이트
                val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
                currentList.removeAll { it.scheduleId == alarm.scheduleId }
                _alarms.value = currentList
                applyPetFilter()
            } catch (e: Exception) {
                android.util.Log.e("AlarmViewModel", "Error deleting alarm", e)
                _error.value = "알람 삭제 중 오류가 발생했습니다"
            }
        }
    }
}