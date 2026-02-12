package com.example.momenty.global.di

import android.content.Context
import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.profile.ProfileApi
import com.example.momenty.data.remote.profile.ProfileImageApi
import com.example.momenty.data.repository.AuthRepository
import com.example.momenty.data.repository.PresignedImageRepository
import com.example.momenty.data.repository.ProfileRepository
import com.example.momenty.global.security.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Repository 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // ========================================
    // 🔐 인증 관련 Repository
    // ========================================

    /**
     * AuthRepository 제공
     * 소셜 로그인 (Kakao, Google, Naver) 및 로그아웃 처리
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context,
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(context, authApi, tokenManager)
    }

    // ========================================
    // 🖼️ 프로필 이미지 업로드 (Presigned URL 방식)
    // ========================================

    /**
     * PresignedImageRepository 제공
     * S3 직접 업로드를 위한 Repository
     *
     * 주의: ProfileImageApi는 ProfileImageModule에서 제공
     * 주의: OkHttpClient는 NetworkModule에서 제공
     */
    @Provides
    @Singleton
    fun providePresignedImageRepository(
        profileImageApi: ProfileImageApi,  // ProfileImageModule에서 제공
        okHttpClient: OkHttpClient  // NetworkModule에서 제공
    ): PresignedImageRepository {
        return PresignedImageRepository(profileImageApi, okHttpClient)
    }

    /**
     * ProfileRepository 제공
     * 프로필 업데이트 API 호출
     */
    @Provides
    @Singleton
    fun provideProfileRepository(
        profileApi: ProfileApi,
        tokenManager: TokenManager
    ): ProfileRepository {
        return ProfileRepository(profileApi, tokenManager)
    }

    // ========================================
    // 📝 기타 Repository (추후 추가)
    // ========================================

    // TODO: 나머지 Repository들 추가
    // HomeRepository, ScheduleRepository, QuestionRepository,
    // ChatbotRepository, MomentRepository, CommunityRepository

    // 예시:
    // @Provides
    // @Singleton
    // fun provideScheduleRepository(
    //     scheduleApi: ScheduleApi,
    //     tokenManager: TokenManager
    // ): ScheduleRepository {
    //     return ScheduleRepositoryImpl(scheduleApi, tokenManager)
    // }
}