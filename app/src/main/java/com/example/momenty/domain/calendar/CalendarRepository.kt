package com.example.momenty.domain.calendar

import android.util.Log
import com.example.momenty.domain.calendar.api.request.AlarmStatusRequest
import com.example.momenty.domain.calendar.api.request.CreateScheduleRequest
import com.example.momenty.domain.calendar.api.response.AlarmDto
import com.example.momenty.domain.calendar.api.response.DailyScheduleResponse
import com.example.momenty.domain.calendar.api.response.MonthlyScheduleResponse
import com.example.momenty.domain.calendar.api.response.PetMonthlyScheduleResponse
import com.example.momenty.global.security.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 캘린더 Repository - ANR 방지 및 에러 처리 개선
 * 서버에서 실제 반려동물 데이터 로드
 */
class CalendarRepository(
    private val calendarApiService: CalendarApiService,
    private val petApiService: PetApiService,
    // private val deviceCalendarHelper: DeviceCalendarHelper,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "CalendarRepository"
        private const val NETWORK_TIMEOUT = 30000L // 30초로 증가
        private const val IMAGE_BASE_URL =
            "https://your-s3-bucket.s3.amazonaws.com/" // S3 버킷 URL로 변경 필요
    }

    /**
     * 서버에서 사용자의 반려동물 목록 가져오기
     * Timeout 설정으로 ANR 방지
     */
    suspend fun getUserPets(userId: Long): List<Pet> = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = petApiService.getUserPets(userId, token)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.pets.map { dto ->
                    Pet(
                        petId = dto.petId,
                        profile = dto.petProfile
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pets", e)
            // 에러 발생 시 빈 리스트 반환
            emptyList()
        }
    }

    /**
     * 디바이스 캘린더 목록 가져오기
     * suspend fun getDeviceCalendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
     *         try {
     *             val calendars = deviceCalendarHelper.getAvailableCalendars()
     *
     *             calendars.map { deviceCal ->
     *                 DeviceCalendar(
     *                     id = deviceCal.id,
     *                     name = deviceCal.name,
     *                     accountName = deviceCal.accountName,
     *                     isSelected = false
     *                 )
     *             }
     *         } catch (e: Exception) {
     *             Log.e(TAG, "Error getting device calendars", e)
     *             emptyList()
     *         }
     *     }
     */

    /**
     * 모든 반려동물 월별 일정 조회
     */
    suspend fun getAllPetsMonthlySchedules(
        userId: Long,
        year: Int,
        month: Int
    ): MonthlyScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.getAllPetsMonthlySchedules(
                userId, year, month, token
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching monthly schedules", e)
            null
        }
    }

    /**
     * 반려동물별 월별 일정 조회
     */
    suspend fun getPetMonthlySchedules(
        userId: Long,
        petId: Long,
        year: Int,
        month: Int
    ): PetMonthlyScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.getPetMonthlySchedules(
                userId, petId, year, month, token
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pet monthly schedules", e)
            null
        }
    }

    /**
     * 반려동물별 일별 일정 조회
     */
    suspend fun getPetDailySchedules(
        petId: Long,
        date: String  // "YYYY-MM-DD"
    ): DailyScheduleResponse? = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.getPetDailySchedules(
                petId, date, token
            )

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily schedules", e)
            null
        }
    }

    /**
     * 일정 생성
     */
    suspend fun createSchedule(
        petId: Long,
        request: CreateScheduleRequest
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.createSchedule(
                petId, request, token
            )

            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error creating schedule", e)
            false
        }
    }

    /**
     * 알림 목록 조회
     */
    suspend fun getAlarms(petId: Long): List<AlarmDto> = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.getAlarms(petId, token)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                response.body()?.result?.alarms ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching alarms", e)
            emptyList()
        }
    }

    /**
     * 알림 ON/OFF 토글
     */
    suspend fun toggleAlarmStatus(
        scheduleId: Long,
        isEnabled: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = calendarApiService.toggleAlarmStatus(
                scheduleId,
                AlarmStatusRequest(isEnabled),
                token
            )

            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling alarm status", e)
            false
        }
    }
}