package com.example.momenty.domain.calendar

import com.example.momenty.domain.calendar.api.response.PetScheduleDto
import com.example.momenty.global.api.BaseResponse
import retrofit2.http.GET

/**
 * 반려동물 API 서비스
 */
interface PetApiService {
    /**
     * 사용자의 반려동물 목록 조회
     */
    @GET("api/pets/my")
    suspend fun getUserPets(): BaseResponse<List<PetScheduleDto>>
}