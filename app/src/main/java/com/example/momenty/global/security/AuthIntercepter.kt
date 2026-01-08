package com.example.momenty.global.security

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * JWT 토큰 자동 추가 인터셉터
 * 모든 API 요청 헤더에 자동으로 토큰 추가
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 토큰이 없으면 원본 요청 그대로 전송
        val token = tokenManager.getAccessToken()
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // 토큰이 있으면 Authorization 헤더 추가
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}