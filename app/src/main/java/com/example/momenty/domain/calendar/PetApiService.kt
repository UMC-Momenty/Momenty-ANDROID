package com.example.momenty.domain.calendar

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
    @GET("api/users/{userId}/schedules/pets")
    suspend fun getUserPets(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String // Bearer 토큰
    ): Response<PetListResponse>
}

/**
 * 반려동물 목록 응답
 */
data class PetListResponse(
    val pets: List<PetDto>
)

/**
 * 반려동물 DTO
 */
data class PetDto(
    val petId: Long,
    val petProfile: String // 이미지 URL
)