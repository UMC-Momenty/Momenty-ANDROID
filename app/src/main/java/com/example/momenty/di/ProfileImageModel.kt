package com.example.momenty.di

import com.example.momenty.data.remote.profile.ProfileImageApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileImageModule {

    /**
     * ProfileImageApi 제공
     */
    @Provides
    @Singleton
    fun provideProfileImageApi(retrofit: Retrofit): ProfileImageApi {
        return retrofit.create(ProfileImageApi::class.java)
    }
}