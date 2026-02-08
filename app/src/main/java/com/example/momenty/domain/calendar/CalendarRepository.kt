package com.example.momenty.domain.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class CalendarRepository(
    private val apiService: CalendarApiService,
    private val deviceCalendarHelper: DeviceCalendarHelper
) {
    /**
     * 서버에서 반려동물 목록 가져오기
     */
    suspend fun getPets(): Result<List<Pet>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPets()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 디바이스 캘린더 목록 가져오기
     */
    suspend fun getAvailableCalendars(): Result<List<DeviceCalendarHelper.DeviceCalendar>> =
        withContext(Dispatchers.IO) {
            try {
                val calendars = deviceCalendarHelper.getAvailableCalendars()
                Result.success(calendars)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * 특정 기간의 일정 가져오기
     */
    suspend fun getEventsInRange(
        startDate: Long,
        endDate: Long,
        selectedPetId: String? = null
    ): Result<List<CalendarEvent>> = withContext(Dispatchers.IO) {
        try {
            val events = deviceCalendarHelper.getEventsInRange(startDate, endDate)

            // 반려동물 필터링
            val filteredEvents = if (selectedPetId != null) {
                events.filter { it.petId == selectedPetId }
            } else {
                events
            }

            Result.success(filteredEvents)
        } catch (e: Exception) {
            Result.failure(e)
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
    ): Result<Long> = withContext(Dispatchers.IO) {
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
                // 서버에도 일정 정보 동기화 (선택사항)
                petId?.let {
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
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to add event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val success = deviceCalendarHelper.updateEvent(
                eventId = eventId,
                title = title,
                startTime = startTime,
                endTime = endTime,
                description = description
            )

            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 일정 삭제
     */
    suspend fun deleteEvent(eventId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val success = deviceCalendarHelper.deleteEvent(eventId)

            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// API 요청용 데이터 클래스
data class CalendarEventRequest(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val description: String?,
    val petId: String
)