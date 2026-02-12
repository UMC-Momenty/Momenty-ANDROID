package com.example.momenty

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Momenty Application
 *
 * 최적화 포인트:
 * - SDK 초기화를 백그라운드 스레드로 이동
 * - 앱 시작 시간 최소화
 * - ANR 방지
 */
@HiltAndroidApp
class MomentyApplication : Application() {

    companion object {
        private const val TAG = "MomentyApplication"
    }

    // Application 스코프 코루틴
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Application 시작")

        // 백그라운드에서 SDK 초기화
        initializeSdksAsync()
    }

    /**
     * 비동기로 SDK 초기화
     * 메인 스레드 블로킹 방지
     */
    private fun initializeSdksAsync() {
        applicationScope.launch {
            try {
                // 카카오 SDK 초기화
                initKakaoSdk()

                // 네이버 SDK 초기화
                initNaverSdk()

                Log.d(TAG, "모든 SDK 초기화 완료")

            } catch (e: Exception) {
                Log.e(TAG, "SDK 초기화 중 오류 발생", e)
                // SDK 초기화 실패해도 앱은 계속 실행
                // 해당 소셜 로그인만 비활성화
            }
        }
    }

    /**
     * 카카오 SDK 초기화
     */
    private fun initKakaoSdk() {
        try {
            KakaoSdk.init(
                context = this,
                appKey = BuildConfig.KAKAO_NATIVE_APP_KEY
            )
            Log.d(TAG, "카카오 SDK 초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "카카오 SDK 초기화 실패", e)
        }
    }

    /**
     * 네이버 SDK 초기화
     */
    private fun initNaverSdk() {
        try {
            NaverIdLoginSDK.initialize(
                context = this,
                clientId = BuildConfig.NAVER_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_CLIENT_SECRET,
                clientName = getString(R.string.app_name)
            )
            Log.d(TAG, "네이버 SDK 초기화 완료")
        } catch (e: Exception) {
            Log.e(TAG, "네이버 SDK 초기화 실패", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application 종료")
    }
}