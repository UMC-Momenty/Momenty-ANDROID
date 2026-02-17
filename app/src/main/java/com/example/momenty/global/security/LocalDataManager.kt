package com.example.momenty.global.security

import android.content.Context
import android.content.SharedPreferences
import com.example.momenty.global.mock.LocalPet
import com.example.momenty.global.mock.LocalSchedule
import com.example.momenty.global.mock.LocalUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 로컬 데이터 관리 헬퍼 클래스
 * SharedPreferences를 사용하여 사용자, 반려동물, 일정 데이터를 관리
 */
class LocalDataManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_USER = "user_info"
        private const val KEY_PETS = "pets_list"
        private const val KEY_SCHEDULES = "schedules_list"
    }

    // ==================== 사용자 관리 ====================

    /**
     * 사용자 정보 저장
     */
    fun saveUser(user: LocalUser) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_USER, json).apply()
    }

    /**
     * 사용자 정보 조회
     */
    fun getUser(): LocalUser? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, LocalUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 사용자 정보 삭제
     */
    fun clearUser() {
        prefs.edit().remove(KEY_USER).apply()
    }

    // ==================== 반려동물 관리 ====================

    /**
     * 반려동물 목록 저장
     */
    fun savePets(pets: List<LocalPet>) {
        val json = gson.toJson(pets)
        prefs.edit().putString(KEY_PETS, json).apply()
    }

    /**
     * 반려동물 목록 조회
     */
    fun getPets(): List<LocalPet> {
        val json = prefs.getString(KEY_PETS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LocalPet>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 반려동물 추가
     */
    fun addPet(pet: LocalPet) {
        val pets = getPets().toMutableList()
        pets.add(pet)
        savePets(pets)
    }

    /**
     * 반려동물 업데이트
     */
    fun updatePet(updatedPet: LocalPet) {
        val pets = getPets().toMutableList()
        val index = pets.indexOfFirst { it.petId == updatedPet.petId }
        if (index != -1) {
            pets[index] = updatedPet
            savePets(pets)
        }
    }

    /**
     * 반려동물 삭제
     */
    fun deletePet(petId: Long) {
        val pets = getPets().filter { it.petId != petId }
        savePets(pets)
    }

    /**
     * 반려동물 조회 (ID로)
     */
    fun getPetById(petId: Long): LocalPet? {
        return getPets().find { it.petId == petId }
    }

    // ==================== 일정 관리 ====================

    /**
     * 일정 목록 저장
     */
    fun saveSchedules(schedules: List<LocalSchedule>) {
        val json = gson.toJson(schedules)
        prefs.edit().putString(KEY_SCHEDULES, json).apply()
    }

    /**
     * 일정 목록 조회
     */
    fun getSchedules(): List<LocalSchedule> {
        val json = prefs.getString(KEY_SCHEDULES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LocalSchedule>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 일정 추가
     */
    fun addSchedule(schedule: LocalSchedule) {
        val schedules = getSchedules().toMutableList()
        schedules.add(schedule)
        saveSchedules(schedules)
    }

    /**
     * 일정 업데이트
     */
    fun updateSchedule(updatedSchedule: LocalSchedule) {
        val schedules = getSchedules().toMutableList()
        val index = schedules.indexOfFirst { it.scheduleId == updatedSchedule.scheduleId }
        if (index != -1) {
            schedules[index] = updatedSchedule
            saveSchedules(schedules)
        }
    }

    /**
     * 일정 삭제
     */
    fun deleteSchedule(scheduleId: Long) {
        val schedules = getSchedules().filter { it.scheduleId != scheduleId }
        saveSchedules(schedules)
    }

    /**
     * 일정 조회 (ID로)
     */
    fun getScheduleById(scheduleId: Long): LocalSchedule? {
        return getSchedules().find { it.scheduleId == scheduleId }
    }

    /**
     * 특정 반려동물의 일정 조회
     */
    fun getSchedulesByPetId(petId: Long): List<LocalSchedule> {
        return getSchedules().filter { it.petId == petId }
    }

    /**
     * 특정 날짜의 일정 조회
     */
    fun getSchedulesByDate(date: String): List<LocalSchedule> {
        return getSchedules().filter {
            it.isOneTime && it.date == date
        }
    }

    /**
     * 알림이 활성화된 일정만 조회
     */
    fun getAlarmsEnabled(): List<LocalSchedule> {
        return getSchedules().filter { it.isAlarmEnabled }
    }

    /**
     * 알림 상태 토글
     */
    fun toggleAlarmStatus(scheduleId: Long, isEnabled: Boolean) {
        val schedules = getSchedules().toMutableList()
        val index = schedules.indexOfFirst { it.scheduleId == scheduleId }
        if (index != -1) {
            schedules[index] = schedules[index].copy(isAlarmEnabled = isEnabled)
            saveSchedules(schedules)
        }
    }

    // ==================== 전체 데이터 관리 ====================

    /**
     * 모든 데이터 삭제
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * 샘플 데이터 생성 (테스트용)
     */
    fun createSampleData() {
        // 샘플 사용자
        val user = LocalUser(
            userId = 1L,
            userName = "테스트 사용자",
            userEmail = "test@momenty.com"
        )
        saveUser(user)

        // 샘플 반려동물
        val pets = listOf(
            LocalPet(
                petId = 1L,
                petName = "뭉치",
                petImageUrl = "https://example.com/pet1.jpg",
                petType = "DOG",
                petBreed = "포메라니안",
                petGender = "MALE",
                petBirthDate = "2020-03-10",
                petIntroduction = "귀여운 우리 강아지"
            ),
            LocalPet(
                petId = 2L,
                petName = "나비",
                petImageUrl = "https://example.com/pet2.jpg",
                petType = "CAT",
                petBreed = "코리안숏헤어",
                petGender = "FEMALE",
                petBirthDate = "2019-07-22",
                petIntroduction = "도도한 우리 고양이"
            ),
            LocalPet(
                petId = 3L,
                petName = "초코",
                petImageUrl = "https://example.com/pet3.jpg",
                petType = "DOG",
                petBreed = "골든리트리버",
                petGender = "MALE",
                petBirthDate = "2021-01-15",
                petIntroduction = "순한 우리 대형견"
            )
        )
        savePets(pets)

        // 샘플 일정
        val schedules = listOf(
            LocalSchedule(
                scheduleId = 1L,
                petId = 1L,
                title = "산책",
                category = "WALK",
                repeatDays = listOf("MONDAY", "WEDNESDAY", "FRIDAY"),
                time = "09:00",
                durationMinutes = 30,
                memo = "아침 산책",
                isAlarmEnabled = true,
                isOneTime = false
            ),
            LocalSchedule(
                scheduleId = 2L,
                petId = 1L,
                title = "식사",
                category = "MEAL",
                repeatDays = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"),
                time = "08:00",
                durationMinutes = 15,
                isAlarmEnabled = true,
                isOneTime = false
            ),
            LocalSchedule(
                scheduleId = 3L,
                petId = 2L,
                title = "병원 방문",
                category = "HEALTH",
                date = "2026-02-20",
                time = "14:00",
                durationMinutes = 60,
                memo = "예방접종",
                isAlarmEnabled = true,
                isOneTime = true
            ),
            LocalSchedule(
                scheduleId = 4L,
                petId = 3L,
                title = "미용",
                category = "BEAUTY",
                date = "2026-02-25",
                time = "15:00",
                durationMinutes = 120,
                memo = "털 정리",
                isAlarmEnabled = true,
                isOneTime = true
            )
        )
        saveSchedules(schedules)
    }
}