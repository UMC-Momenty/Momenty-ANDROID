// global/security/AuthInterceptor.kt
package com.example.momenty.global.security

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()


        val url = originalRequest.url
        val host = url.host


        if (host.contains("amazonaws.com")) {
            return chain.proceed(originalRequest)
        }

        // 토큰이 필요 없는 엔드포인트 (로그인, 회원가입 등)
        val noAuthPaths = listOf("/auth/login", "/auth/signup", "/auth/refresh")
        val isNoAuthPath = noAuthPaths.any { originalRequest.url.encodedPath.contains(it) }

        if (isNoAuthPath) {
            return chain.proceed(originalRequest)
        }

        // JWT 토큰 추가
        val token = tokenManager.getAccessToken()
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}