package com.example.momenty.data.remote.auth

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/kakao/login")
    suspend fun kakaoLogin(
        @Body request: KakaoLoginRequest
    ): BaseResponse<LoginResponse>

    @POST("auth/google/login")
    suspend fun googleLogin(
        @Body request: GoogleLoginRequest
    ): BaseResponse<LoginResponse>
}

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val firebaseCustomToken: String
)