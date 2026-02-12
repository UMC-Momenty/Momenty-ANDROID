package com.example.momenty.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Firebase Module
 *
 * Firebase는 FCM(푸시 알림) 용도로만 사용
 * 인증은 백엔드 JWT 사용
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * FCM (Firebase Cloud Messaging)
     * 푸시 알림 수신용
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging =
        FirebaseMessaging.getInstance()
}