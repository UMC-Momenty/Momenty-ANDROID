package com.example.momenty.data.remote.auth

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    /**
     * 통합 소셜 로그인 엔드포인트
     * provider: KAKAO, GOOGLE, NAVER
     */
    @POST("api/oauth/login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): BaseResponse<LoginResponse>

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급
     */
    @POST("api/oauth/reissue")
    suspend fun reissueToken(
        @Body request: ReissueRequest
    ): BaseResponse<LoginResponse>
}

/**
 * 통합 소셜 로그인 요청
 */
data class SocialLoginRequest(
    val provider: String,  // "KAKAO", "GOOGLE", "NAVER"
    val accessToken: String
)

/**
 * 토큰 재발급 요청
 */
data class ReissueRequest(
    val refreshToken: String
)

/**
 * 로그인 응답 (백엔드 자체 JWT만 반환)
 */
data class LoginResponse(
    val accessToken: String,   // 백엔드 JWT
    val refreshToken: String,   // 백엔드 Refresh Token
    val userId: Long
)