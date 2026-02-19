package com.example.momenty.domain.calendar

import com.example.momenty.domain.calendar.api.request.AlarmStatusRequest
import com.example.momenty.domain.calendar.api.request.CreateScheduleRequest
import com.example.momenty.domain.calendar.api.response.AlarmListResponse
import com.example.momenty.domain.calendar.api.response.DailyScheduleResponse
import com.example.momenty.domain.calendar.api.response.MonthlyScheduleResponse
import com.example.momenty.domain.calendar.api.response.PetMonthlyScheduleResponse
import retrofit2.Response
import retrofit2.http.*
interface CalendarApiService {

    /**
     * 2. 모든 반려동물 달별 일정 조회
     */
    @GET("api/users/schedules/calendar")
    suspend fun getAllPetsMonthlySchedules(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MonthlyScheduleResponse>

    /**
     * 3. 반려동물별 달별 일정 조회
     */
    @GET("api/users/schedules/pets/{petId}/calendar")
    suspend fun getPetMonthlySchedules(
        @Path("petId") petId: Long,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<PetMonthlyScheduleResponse>

    /**
     * 4. 반려동물별 일별 일정 조회
     */
    @GET("api/pets/{petId}/schedules")
    suspend fun getPetDailySchedules(
        @Path("petId") petId: Long,
        @Query("date") date: String  // "YYYY-MM-DD" 형식
    ): Response<DailyScheduleResponse>

    /**
     * 5. 반려동물별 일정 및 알림 생성
     */
    @POST("api/pets/{petId}/schedules")
    suspend fun createSchedule(
        @Path("petId") petId: Long,
        @Body request: CreateScheduleRequest
    ): Response<Unit>  // 또는 생성된 일정 반환

    /**
     * 6. 알림 목록 조회
     */
    @GET("api/pets/{petId}/alarms")
    suspend fun getAlarms(
        @Path("petId") petId: Long
    ): Response<AlarmListResponse>

    /**
     * 7. 알림 ON/OFF 토글
     */
    @PATCH("api/schedules/{scheduleId}/alarm-status")
    suspend fun toggleAlarmStatus(
        @Path("scheduleId") scheduleId: Long,
        @Body request: AlarmStatusRequest
    ): Response<Unit>
}