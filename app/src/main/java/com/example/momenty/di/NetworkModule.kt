package com.example.momenty.di

import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.profile.ProfileApi
import com.example.momenty.global.mock.MockApiInterceptor
import com.example.momenty.global.security.AuthInterceptor
import com.kakao.sdk.v2.auth.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.momenty.com/" // TODO: 실제 URL로 변경

    /**
     * Mock API 사용 여부
     * build.gradle에서 USE_MOCK_API = true로 설정하면 활성화
     */
    private val useMockApi: Boolean
        get() = false // 디버그 빌드에서만 Mock 사용 가능

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Mock Interceptor 제공
     */
    @Provides
    @Singleton
    fun provideMockApiInterceptor(): MockApiInterceptor {
        return MockApiInterceptor().apply {
            // Mock 활성화 여부 설정
            MockApiInterceptor.isMockEnabled = useMockApi
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        mockApiInterceptor: MockApiInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                // Mock API가 활성화되어 있으면 맨 앞에 추가
                if (useMockApi) {
                    addInterceptor(mockApiInterceptor)
                }
            }
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }
}