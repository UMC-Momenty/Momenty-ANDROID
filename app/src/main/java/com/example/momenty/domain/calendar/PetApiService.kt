package com.example.momenty.domain.calendar

import retrofit2.Response
import retrofit2.http.GET

/**
 * 반려동물 API 서비스
 */
interface PetApiService {
    /**
     * 사용자의 반려동물 목록 조회
     */
    @GET("api/pets")
    suspend fun getUserPets(): Response<PetListResponse>
}

/**
 * 반려동물 목록 응답
 */
data class PetListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<PetDto>?
)

/**
 * 반려동물 DTO
 */
data class PetDto(
    val petId: String,
    val petName: String,
    val petImageKey: String?,  // S3 이미지 키
    val petType: String,       // 예: "DOG", "CAT"
    val petBreed: String?,
    val petGender: String,
    val petBirthDate: String,
    val petIntroduction: String?
)