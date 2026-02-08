package com.example.momenty.domain.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.momenty.domain.main.presentation.MainActivity
import com.example.momenty.global.security.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.example.momenty.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Screen API 설치
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        // 로그인 상태 확인 및 화면 이동
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        lifecycleScope.launch {
            // 스플래시 최소 표시 시간
            delay(1500)

            // 임시 강제 로그아웃
            tokenManager.clearTokens()
            firebaseAuth.signOut()

            // JWT 토큰과 Firebase 인증 상태 확인
            val hasToken = tokenManager.isLoggedIn()
            val hasFirebaseUser = firebaseAuth.currentUser != null

            val intent = Intent(this@SplashActivity, MainActivity::class.java)

            if (hasToken && hasFirebaseUser) {
                // 프로필 설정 완료 여부 확인
                val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
                val isProfileCompleted = prefs.getBoolean("profile_completed", false)

                if(isProfileCompleted)  {
                    // 로그인 상태 → 메인 화면으로 (RecordFragment)
                    intent.putExtra("navigate_to", "home")
                }else   {
                    intent.putExtra("navigate_to", "profile")
                }
            } else {
                // 비로그인 상태 → 약관 동의 화면으로
                intent.putExtra("navigate_to", "terms")
                // 기존 토큰 정리 (불완전한 로그인 상태 방지)
                tokenManager.clearTokens()
            }

            startActivity(intent)
            finish()
        }
    }
}