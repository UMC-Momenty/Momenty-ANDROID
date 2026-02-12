package com.example.momenty.data.remote.profile

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ProfileApi {

    /**
     * 사용자 및 반려동물 프로필 업데이트
     * Authorization: Bearer {accessToken}
     */
    @PATCH("api/mypage")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): BaseResponse<UpdateProfileResponse>
}

/**
 * 프로필 업데이트 요청
 */
data class UpdateProfileRequest(
    val userName: String,
    val userGender: String,
    val userBirthDate: String,
    val userImageKey: String?,      // Presigned URL로 업로드 후 받은 key
    val alarmTime: String?,
    val petName: String,
    val petGender: String,
    val petBirthDate: String,
    val petImageKey: String?,       // 반려동물 이미지 key
    val petType: String,
    val petBreed: String?,
    val petIntroduction: String?
)

/**
 * 프로필 업데이트 응답
 */
data class UpdateProfileResponse(
    val userId: String,
    val userName: String,
    val petId: String,
    val petName: String
)