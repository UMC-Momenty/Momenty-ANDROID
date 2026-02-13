package com.example.momenty.domain.calendar

import android.content.Context
import com.example.momenty.global.mock.MockApiInterceptor
import com.example.momenty.global.security.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 실제 서버 URL
    private const val BASE_URL = "https://api.momenty.com/"

    // TokenManager 인스턴스
    private lateinit var tokenManager: TokenManager

    // Context 인스턴스 (MockApiInterceptor에 필요)
    private lateinit var appContext: Context

    /**
     * TokenManager와 Context 초기화
     * Fragment의 onViewCreated나 Application에서 호출
     *
     * @param tokenManager 토큰 매니저
     * @param context Application Context 또는 Fragment Context
     */
    fun initialize(tokenManager: TokenManager, context: Context) {
        this.tokenManager = tokenManager
        // Application Context로 변환하여 메모리 누수 방지
        this.appContext = context.applicationContext
    }

    /**
     * Context만 초기화 (TokenManager는 이미 초기화된 경우)
     */
    fun initializeContext(context: Context) {
        this.appContext = context.applicationContext
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Mock API 인터셉터
     * Context가 필요하므로 초기화 확인 필요
     */
    private val mockApiInterceptor by lazy {
        if (!::appContext.isInitialized) {
            throw IllegalStateException(
                "RetrofitClient가 초기화되지 않았습니다. " +
                        "RetrofitClient.initialize(tokenManager, context)를 먼저 호출하세요."
            )
        }

        MockApiInterceptor(appContext).apply {
            // Mock 모드 활성화 (개발 중에는 true, 배포 시에는 false)
            MockApiInterceptor.isMockEnabled = true
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
            .addInterceptor(mockApiInterceptor)   // Mock 인터셉터 (가장 먼저!)
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

    /**
     * Mock 모드 활성화/비활성화
     * @param enabled true: Mock 사용 (SharedPreferences), false: 실제 서버 사용
     */
    fun setMockEnabled(enabled: Boolean) {
        MockApiInterceptor.isMockEnabled = enabled
    }

    /**
     * 현재 Mock 모드 상태 확인
     */
    fun isMockEnabled(): Boolean {
        return MockApiInterceptor.isMockEnabled
    }
}