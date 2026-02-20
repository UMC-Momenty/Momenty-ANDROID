package com.example.momenty.domain.calendar

import android.util.Log
import com.example.momenty.domain.calendar.api.request.AlarmStatusRequest
import com.example.momenty.domain.calendar.api.request.CreateScheduleRequest
import com.example.momenty.domain.calendar.api.response.CreateScheduleResponse
import com.example.momenty.domain.calendar.api.response.MonthlyScheduleResponse
import com.example.momenty.domain.calendar.api.response.PetMonthlyScheduleResponse
import com.example.momenty.global.security.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 캘린더 Repository - 백엔드 API 기반
 */
class CalendarRepository(
    private val calendarApiService: CalendarApiService,
    private val petApiService: PetApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "CalendarRepository"
        private const val NETWORK_TIMEOUT = 30000L // 30초
    }

    /**
     * 1. 사용자의 반려동물 목록 가져오기
     */
    suspend fun getPets(): List<Pet> = withContext(Dispatchers.IO) {
        try {

            // 로그인 확인
            if (!tokenManager.isLoggedIn()) {
                Log.w(TAG, "User not logged in")
                return@withContext emptyList()
            }

            // 네트워크 요청
            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                petApiService.getUserPets()
            }

            if (response?.isSuccessful == true && response.body() != null) {
                response.body()!!.pets?.map { dto ->
                    Pet(
                        petId = dto.petId,
                        profile = dto.profile
                    )
                } ?: emptyList()
            } else {
                Log.w(TAG, "Failed to fetch pets: code=${response?.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pets", e)
            emptyList()
        }
    }

    /**
     * 2. 모든 반려동물의 월별 일정 조회
     */
    suspend fun getAllPetsMonthlySchedules(
        year: Int,
        month: Int
    ): MonthlyScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            if(!tokenManager.isLoggedIn()){
                Log.w(TAG, "USer not logged in")
                return@withContext null
            }

            Log.d(TAG, "Fetching all pets monthly schedules: year=$year, month=$month")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.getAllPetsMonthlySchedules(year, month)
            }

            if (response?.isSuccessful == true) {
                Log.d(TAG, "Monthly schedules loaded successfully")
                response.body()
            } else {
                Log.w(TAG, "Failed to fetch monthly schedules: code=${response?.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching monthly schedules", e)
            null
        }
    }

    /**
     * 3. 특정 반려동물의 월별 일정 조회
     */
    suspend fun getPetMonthlySchedules(
        petId: Long,
        year: Int,
        month: Int
    ): PetMonthlyScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            if(!tokenManager.isLoggedIn()){
                Log.w(TAG, "USer not logged in")
                return@withContext null
            }

            Log.d(TAG, "Fetching pet monthly schedules: petId=$petId, year=$year, month=$month")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.getPetMonthlySchedules(petId, year, month)
            }

            if (response?.isSuccessful == true) {
                Log.d(TAG, "Pet monthly schedules loaded successfully")
                response.body()
            } else {
                Log.w(TAG, "Failed to fetch pet monthly schedules: code=${response?.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pet monthly schedules", e)
            null
        }
    }

    /**
     * 4. 특정 반려동물의 일별 일정 조회
     */
    suspend fun getPetDailySchedules(
        petId: Long,
        date: String  // "YYYY-MM-DD" 형식
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching daily schedules: petId=$petId, date=$date")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.getPetDailySchedules(petId, date)
            }

            if (response?.isSuccessful == true && response.body() != null) {
                val schedules = response.body()!!.schedules.map { dto ->
                    CalendarEvent(
                        scheduleId = dto.scheduleId,
                        petId = petId,
                        title = dto.title,
                        startAt = dto.startAt,
                        memo = dto.memo,
                        category = dto.category,
                        durationMinutes = dto.durationMinutes,
                        isAlarmEnabled = dto.isAlarmEnabled
                    )
                }
                Log.d(TAG, "Loaded ${schedules.size} schedules for date=$date")
                schedules
            } else {
                Log.w(TAG, "Failed to fetch daily schedules: code=${response?.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily schedules", e)
            emptyList()
        }
    }

    /**
     * 5. 일정 생성
     */
    suspend fun createSchedule(
        petId: Long,
        request: CreateScheduleRequest
    ): CreateScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating schedule for petId=$petId: ${request.title}")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.createSchedule(petId, request)
            }

            if (response?.isSuccessful == true) {
                Log.d(TAG, "Schedule created successfully")
                // Mock API는 Unit을 반환하므로, 성공 시 임시 응답 생성
                CreateScheduleResponse(
                    scheduleId = System.currentTimeMillis().toString(),
                    title = request.title
                )
            } else {
                Log.w(TAG, "Failed to create schedule: code=${response?.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating schedule", e)
            null
        }
    }

    /**
     * 6. 알림 목록 조회
     */
    suspend fun getAlarms(petId: Long): List<Alarm> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching alarms for petId=$petId")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.getAlarms(petId)
            }

            if (response?.isSuccessful == true &&
                response.body()?.isSuccess == true &&
                response.body()?.result != null) {

                response.body()!!.result.alarms.map { dto ->
                    Alarm(
                        scheduleId = dto.scheduleId,
                        title = dto.title,
                        category = dto.category,
                        petId = petId,
                        isOneTime = dto.isOneTime == true,
                        repeatDays = dto.repeatDays,
                        date = dto.date,
                        alarmTime = dto.alarmTime,
                        durationMinutes = dto.durationMinutes ?: 0,
                        isAlarmEnabled = dto.isAlarmEnabled
                    )
                }
            } else {
                Log.w(TAG, "Failed to fetch alarms: code=${response?.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching alarms", e)
            emptyList()
        }
    }

    /**
     * 7. 알림 ON/OFF 토글
     */
    suspend fun toggleAlarmStatus(
        scheduleId: Long,
        isEnabled: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Toggling alarm status: scheduleId=$scheduleId, isEnabled=$isEnabled")

            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                calendarApiService.toggleAlarmStatus(
                    scheduleId = scheduleId,
                    request = AlarmStatusRequest(isEnabled)
                )
            }

            val success = response?.isSuccessful == true
            if (success) {
                Log.d(TAG, "Alarm status toggled successfully")
            } else {
                Log.w(TAG, "Failed to toggle alarm status: code=${response?.code()}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling alarm status", e)
            false
        }
    }

    /**
     * 캐시 클리어 (필요시)
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}