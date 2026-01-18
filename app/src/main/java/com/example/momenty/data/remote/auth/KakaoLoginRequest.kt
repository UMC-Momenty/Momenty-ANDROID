package com.example.momenty.data.remote.auth

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

// ===== Request 모델 =====
data class KakaoLoginRequest(
    val accessToken: String
)

// ===== Response 모델 =====
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val userName: String?,
    val userEmail: String?,
    val firebaseCustomToken: String
)

// ===== API 인터페이스 =====
interface AuthApi {

    /**
     * 카카오 소셜 로그인
     * @param request 카카오 Access Token
     * @return JWT 토큰 및 Firebase Custom Token
     */
    @POST("/api/auth/kakao")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest
    ): BaseResponse<LoginResponse>
}