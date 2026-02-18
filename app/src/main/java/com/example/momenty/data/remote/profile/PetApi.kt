package com.example.momenty.data.remote.profile

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Path

interface PetApi {

    /**
     * 반려동물 프로필 수정
     * Authorization: Bearer {accessToken}
     * - 전달되지 않은 필드는 기존 값 유지
     * - 빈 배열이면 사진, 소개글 미설정
     */
    @PATCH("api/users/{userId}/pets/{petId}")
    suspend fun updatePetProfile(
        @Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Body request: UpdatePetProfileRequest
    ): BaseResponse<String>  // result: string
}

/**
 * 반려동물 프로필 수정 요청
 */
data class UpdatePetProfileRequest(
    val profileImageUrl: String? = null,  // 빈 배열이면 미설정
    val petName: String? = null,
    val gender: String? = null,           // "MALE", "FEMALE"
    val birth: String? = null,            // "2000-01-01" 형식
    val species: String? = null,          // "CAT" or "DOG"
    val breedId: Long? = null,
    val intro: String? = null             // 빈 배열이면 미설정
)