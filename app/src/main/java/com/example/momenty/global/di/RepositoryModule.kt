package com.example.momenty.global.di

import com.example.momenty.domain.member.data.api.MemberApi
import com.example.momenty.domain.member.data.repository.MemberRepositoryImpl
import com.example.momenty.domain.member.domain.repository.MemberRepository
import com.example.momenty.global.security.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 관련 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * MemberRepository 제공
     */
    @Provides
    @Singleton
    fun provideMemberRepository(
        memberApi: MemberApi,
        tokenManager: TokenManager
    ): MemberRepository {
        return MemberRepositoryImpl(memberApi, tokenManager)
    }

    // TODO: 나머지 Repository들 추가
    // HomeRepository, ScheduleRepository, QuestionRepository,
    // ChatbotRepository, MomentRepository, CommunityRepository

    // 예시:
    // @Provides
    // @Singleton
    // fun provideScheduleRepository(
    //     scheduleApi: ScheduleApi
    // ): ScheduleRepository {
    //     return ScheduleRepositoryImpl(scheduleApi)
    // }
}