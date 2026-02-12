package com.example.momenty.domain.calendar

import com.example.momenty.global.security.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // TokenManager 인스턴스 (Hilt로 주입받거나 싱글톤으로 접근)
    // 주의: 실제 프로젝트에서는 Hilt를 통해 주입받는 것을 권장
    private lateinit var tokenManager: TokenManager

    /**
     * TokenManager 초기화 (Application에서 호출)
     */
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * 인증 토큰 추가 인터셉터
     */
    private val authInterceptor by lazy {
        Interceptor { chain ->
            val originalRequest = chain.request()

            // TokenManager가 초기화되지 않았거나 토큰이 없으면 원본 요청 그대로 전달
            if (!::tokenManager.isInitialized) {
                return@Interceptor chain.proceed(originalRequest)
            }

            val accessToken = tokenManager.getAccessToken()

            // 토큰이 있으면 헤더에 추가
            val newRequest = if (!accessToken.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)      // 인증 인터셉터 추가
            .addInterceptor(loggingInterceptor)   // 로깅 인터셉터
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val calendarApiService: CalendarApiService by lazy {
        retrofit.create(CalendarApiService::class.java)
    }

    val petApiService: PetApiService by lazy {
        retrofit.create(PetApiService::class.java)
    }
}