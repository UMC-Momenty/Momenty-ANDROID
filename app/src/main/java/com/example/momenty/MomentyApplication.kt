package com.example.momenty

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 클래스
 * 앱 시작 시 가장 먼저 실행됨
 */
@HiltAndroidApp
class MomentyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 앱 전역 초기화 작업
        // 예: Timber 로깅, Kakao SDK, Firebase 등
    }
}