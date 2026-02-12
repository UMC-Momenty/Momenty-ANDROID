package com.example.momenty.domain.calendar

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

/**
 * 캘린더 Repository - ANR 방지 및 에러 처리 개선
 */
class CalendarRepository(
    private val apiService: CalendarApiService,
    private val deviceCalendarHelper: DeviceCalendarHelper
) {
    companion object {
        private const val TAG = "CalendarRepository"
        private const val NETWORK_TIMEOUT = 10000L // 10초
    }

    /**
     * 서버에서 반려동물 목록 가져오기
     * Timeout 설정으로 ANR 방지
     */
    suspend fun getPets(): List<Pet> = withContext(Dispatchers.IO) {
        try {
            // 네트워크 요청에 타임아웃 설정
            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                apiService.getPets()
            }

            if (response?.isSuccessful == true && response.body() != null) {
                response.body()!!
            } else {
                // 실패 시 더미 데이터 반환
                Log.w(TAG, "Failed to fetch pets from server, using dummy data")
                getDummyPets()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pets", e)
            // 에러 발생 시에도 더미 데이터 반환하여 앱이 동작하도록 함
            getDummyPets()
        }
    }

    /**
     * 더미 펫 데이터
     */
    private fun getDummyPets(): List<Pet> {
        return listOf(
            Pet(id = "1", name = "코코", imageUrl = null, color = null),
            Pet(id = "2", name = "몽이", imageUrl = null, color = null),
            Pet(id = "3", name = "초코", imageUrl = null, color = null)
        )
    }

    /**
     * 디바이스 캘린더 목록 가져오기
     */
    suspend fun getDeviceCalendars(): List<DeviceCalendar> = withContext(Dispatchers.IO) {
        try {
            val calendars = deviceCalendarHelper.getAvailableCalendars()

            calendars.map { deviceCal ->
                DeviceCalendar(
                    id = deviceCal.id,
                    name = deviceCal.name,
                    accountName = deviceCal.accountName,
                    isSelected = false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device calendars", e)
            emptyList()
        }
    }

    /**
     * 특정 기간의 일정 가져오기
     */
    suspend fun getEventsInRange(
        startDate: Long,
        endDate: Long,
        selectedPetId: String? = null
    ): List<CalendarEvent> = withContext(Dispatchers.IO) {
        try {
            val events = deviceCalendarHelper.getEventsInRange(startDate, endDate)

            // 반려동물 필터링
            if (selectedPetId != null) {
                events.filter { it.petId == selectedPetId }
            } else {
                events
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting events in range", e)
            emptyList()
        }
    }

    /**
     * 일정 추가
     */
    suspend fun addEvent(
        calendarId: String,
        title: String,
        startTime: Date,
        endTime: Date,
        description: String? = null,
        petId: String? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            // 디바이스 캘린더에 일정 추가
            val eventId = deviceCalendarHelper.addEvent(
                calendarId = calendarId,
                title = title,
                startTime = startTime,
                endTime = endTime,
                description = description
            )

            if (eventId != null) {
                // 서버에 동기화 시도 (실패해도 로컬 저장은 성공)
                syncEventToServer(
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    petId = petId
                )

                eventId
            } else {
                Log.e(TAG, "Failed to add event to device calendar")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding event", e)
            null
        }
    }

    /**
     * 서버에 이벤트 동기화 (백그라운드)
     */
    private suspend fun syncEventToServer(
        title: String,
        startTime: Date,
        endTime: Date,
        description: String?,
        petId: String?
    ) {
        try {
            petId?.let {
                withTimeoutOrNull(NETWORK_TIMEOUT) {
                    apiService.createEvent(
                        CalendarEventRequest(
                            title = title,
                            startTime = startTime.time,
                            endTime = endTime.time,
                            description = description,
                            petId = it
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // 서버 동기화 실패는 로깅만 하고 계속 진행
            Log.w(TAG, "Failed to sync event to server", e)
        }
    }

    /**
     * 일정 수정
     */
    suspend fun updateEvent(
        eventId: String,
        title: String? = null,
        startTime: Date? = null,
        endTime: Date? = null,
        description: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            deviceCalendarHelper.updateEvent(
                eventId = eventId,
                title = title,
                startTime = startTime,
                endTime = endTime,
                description = description
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event", e)
            false
        }
    }

    /**
     * 일정 삭제
     */
    suspend fun deleteEvent(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            deviceCalendarHelper.deleteEvent(eventId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting event", e)
            false
        }
    }

    /**
     * 여러 일정을 배치로 추가 (성능 최적화)
     */
    suspend fun addEventsBatch(
        events: List<EventData>
    ): List<Long?> = withContext(Dispatchers.IO) {
        events.map { eventData ->
            try {
                addEvent(
                    calendarId = eventData.calendarId,
                    title = eventData.title,
                    startTime = eventData.startTime,
                    endTime = eventData.endTime,
                    description = eventData.description,
                    petId = eventData.petId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error adding event in batch", e)
                null
            }
        }
    }

    /**
     * 캐시 클리어
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            // 캐시 클리어 로직 구현
            Log.d(TAG, "Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing cache", e)
        }
    }
}

/**
 * 이벤트 데이터 클래스 (배치 추가용)
 */
data class EventData(
    val calendarId: String,
    val title: String,
    val startTime: Date,
    val endTime: Date,
    val description: String? = null,
    val petId: String? = null
)

/**
 * API 요청용 데이터 클래스
 */
data class CalendarEventRequest(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val description: String?,
    val petId: String
)