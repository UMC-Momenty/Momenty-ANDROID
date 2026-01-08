package com.example.momenty.global.di

import com.example.momenty.domain.member.data.api.MemberApi
import com.example.momenty.global.config.RetrofitConfig
import com.example.momenty.global.security.AuthInterceptor
import com.example.momenty.global.security.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * AuthInterceptor 제공
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    /**
     * Retrofit 제공
     */
    @Provides
    @Singleton
    fun provideRetrofit(authInterceptor: AuthInterceptor): Retrofit {
        return RetrofitConfig.createRetrofit(authInterceptor)
    }

    /**
     * MemberApi 제공
     */
    @Provides
    @Singleton
    fun provideMemberApi(retrofit: Retrofit): MemberApi {
        return retrofit.create(MemberApi::class.java)
    }

    // TODO: 나머지 API들 추가
    // HomeApi, ScheduleApi, QuestionApi, ChatbotApi, MomentApi, CommunityApi
    // 예시:
    // @Provides
    // @Singleton
    // fun provideHomeApi(retrofit: Retrofit): HomeApi {
    //     return retrofit.create(HomeApi::class.java)
    // }
}