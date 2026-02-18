package com.example.momenty.data.remote.profile

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ProfileApi {

    /**
     * 사용자 프로필 업데이트
     * Authorization: Bearer {accessToken}
     * - 전달되지 않은 필드는 기존 값 유지
     * - profileUrl이 빈 문자열("")이면 null로 초기화
     * - resetQuestTime: true이면 questTime을 null로 초기화
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
    val username: String? = null,
    val gender: String? = null,
    val birth: String? = null,
    val profileUrl: String? = null,
    val questTime: String? = null,
    val resetQuestTime: Boolean? = null
)

/**
 * 프로필 업데이트 응답
 */
data class UpdateProfileResponse(
    val accessToken: String,
    val refreshToken: String
)