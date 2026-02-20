package com.example.momenty.global.config

import com.example.momenty.global.mock.MockApiInterceptor
import com.example.momenty.global.security.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 설정
 */
object RetrofitConfig {

    // TODO: 백엔드 서버 URL로 변경 필요
    private const val BASE_URL = "https://www.dev-service.shop/"

    // ✨ Mock 모드 활성화 여부 (개발 중: true, 배포 시: false)
    var useMockApi: Boolean = false

    // HTTP 로깅 인터셉터 (개발 중 API 요청/응답 확인용)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✨ Mock API 인터셉터
    private val mockApiInterceptor = MockApiInterceptor().apply {
        MockApiInterceptor.isMockEnabled = useMockApi
    }

    // OkHttpClient 설정
    private fun createOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // ✨ Mock 인터셉터를 가장 먼저 추가 (활성화된 경우)
            if (useMockApi) {
                addInterceptor(mockApiInterceptor)
            }
            addInterceptor(authInterceptor)      // 토큰 자동 추가
            addInterceptor(loggingInterceptor)   // 로깅
            connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
            readTimeout(30, TimeUnit.SECONDS)    // 읽기 타임아웃
            writeTimeout(30, TimeUnit.SECONDS)   // 쓰기 타임아웃
        }.build()
    }

    // Retrofit 인스턴스 생성
    fun createRetrofit(authInterceptor: AuthInterceptor): Retrofit {
        // Mock 모드 동기화
        MockApiInterceptor.isMockEnabled = useMockApi

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(authInterceptor))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}