package com.example.momenty.domain.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlarmViewModel : ViewModel() {

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
    * 알람 목록 로드
    */
    fun loadAlarms() {
        // TODO: 실제로는 서버나 로컬 DB에서 데이터를 가져옴
        // 현재는 저장된 리스트 유지
        applyPetFilter()
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
        val selectedPetId = _selectedPet.value?.id

        _filteredAlarms.value = if (selectedPetId == null) {
            // 전체 알람 표시
            currentAlarms
        } else {
            // 선택된 반려동물의 알람만 표시
            currentAlarms.filter { it.petId == selectedPetId }
        }
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
        applyPetFilter()

        // TODO: 서버나 로컬 DB에 상태 저장
    }

    /**
     * 알람 추가
     */
    fun addAlarm(alarm: Alarm) {
        val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
        currentList.add(alarm)
        _alarms.value = currentList
        applyPetFilter()

        // TODO: 서버나 로컬 DB에 저장
        // 알람 리스트로 이동 트리거
        _navigateToAlarmList.value = true
    }

    /**
     * 알람 삭제
     */
    fun deleteAlarm(alarm: Alarm) {
        val currentList = _alarms.value?.toMutableList() ?: mutableListOf()
        currentList.remove(alarm)
        _alarms.value = currentList
        applyPetFilter()

        // TODO: 서버나 로컬 DB에서 삭제
    }
}