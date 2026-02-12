package com.example.momenty.domain.calendar

import retrofit2.Response
import retrofit2.http.*
interface CalendarApiService {
    /**
     * 반려동물 목록 조회
     */
    @GET("api/pets")
    suspend fun getPets(): Response<List<Pet>>

    /**
     * 일정 조회
     */
    @GET("api/calendar/events")
    suspend fun getEvents(
        @Query("startDate") startDate: Long,
        @Query("endDate") endDate: Long,
        @Query("petId") petId: String? = null
    ): Response<List<CalendarEvent>>

    /**
     * 일정 생성
     */
    @POST("api/calendar/events")
    suspend fun createEvent(
        @Body request: CalendarEventRequest
    ): Response<CalendarEvent>

    /**
     * 일정 수정
     */
    @PUT("api/calendar/events/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Body request: CalendarEventRequest
    ): Response<CalendarEvent>

    /**
     * 일정 삭제
     */
    @DELETE("api/calendar/events/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: String
    ): Response<Unit>
}