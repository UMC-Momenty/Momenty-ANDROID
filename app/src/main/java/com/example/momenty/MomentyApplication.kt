package com.example.momenty

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MomentyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 카카오 SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)

        // 디버그 모드에서만 키 해시 출력
        if (BuildConfig.DEBUG) {
            val keyHash = Utility.getKeyHash(this)
            Log.d("KAKAO_KEY_HASH", "Key Hash: $keyHash")
        }
    }
}