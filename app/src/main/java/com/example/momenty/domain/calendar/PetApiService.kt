package com.example.momenty.domain.calendar

import com.example.momenty.domain.calendar.api.response.UserPetsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * 반려동물 API 서비스
 */
interface PetApiService {
    /**
     * 사용자의 반려동물 목록 조회
     */
    @GET("api/schedules/pets")
    suspend fun getUserPets(): Response<UserPetsResponse>
}