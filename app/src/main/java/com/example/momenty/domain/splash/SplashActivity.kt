package com.example.momenty.domain.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.momenty.domain.main.presentation.MainActivity
import com.example.momenty.global.security.TokenManager
import com.example.momenty.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * 스플래시 화면 - ANR 방지 최적화 버전
 *
 * 주요 개선사항:
 * - 모든 I/O 작업을 IO 스레드로 이동
 * - 타임아웃 설정으로 무한 대기 방지
 * - 초기화 실패 시 안전한 폴백 처리
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    companion object {
        private const val TAG = "SplashActivity"
        private const val SPLASH_DELAY_MS = 2000L // 스플래시 최소 표시 시간 (2초)
        private const val INITIALIZATION_TIMEOUT_MS = 5000L // 5초 타임아웃
        private const val PREFS_NAME = "momenty_prefs"
        private const val KEY_PROFILE_COMPLETED = "profile_completed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)
        // 비동기로 초기화 및 네비게이션 처리
        lifecycleScope.launch {
            try {
                withTimeout(INITIALIZATION_TIMEOUT_MS) {
                    checkLoginStatusAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "초기화 실패 - 기본 화면으로 이동", e)
                navigateToTerms() // 실패 시 안전하게 약관 화면으로
            }
        }
    }

    /**
     * 비동기로 로그인 상태 확인
     * IO 스레드에서 실행되어 메인 스레드 차단 방지
     */
    private suspend fun checkLoginStatusAsync() {
        // IO 스레드에서 실행
        withContext(Dispatchers.IO) {
            try {
                // 스플래시 최소 표시 시간
                delay(SPLASH_DELAY_MS)

                // JWT 토큰 확인 (IO 작업)
                val hasToken = tokenManager.isLoggedIn()
                Log.d(TAG, "로그인 상태: $hasToken")

                // 메인 스레드로 돌아와서 네비게이션
                withContext(Dispatchers.Main) {
                    if (hasToken) {
                        navigateBasedOnProfile()
                    } else {
                        navigateToTerms()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "로그인 상태 확인 실패", e)

                // 에러 발생 시 메인 스레드에서 안전하게 처리
                withContext(Dispatchers.Main) {
                    navigateToTerms()
                }
            }
        }
    }

    /**
     * 프로필 완료 여부에 따라 네비게이션
     * IO 스레드에서 호출되므로 SharedPreferences 읽기 안전
     */
    private suspend fun navigateBasedOnProfile() {
        try {
            // IO 스레드에서 SharedPreferences 읽기
            val isProfileCompleted = withContext(Dispatchers.IO) {
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_PROFILE_COMPLETED, false)
            }

            // 메인 스레드로 돌아와서 네비게이션
            withContext(Dispatchers.Main) {
                if (isProfileCompleted) {
                    Log.d(TAG, "프로필 완료 → 홈 화면")
                    navigateToHome()
                } else {
                    Log.d(TAG, "프로필 미완료 → 프로필 설정 화면")
                    navigateToProfile()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "프로필 상태 확인 실패", e)
            navigateToTerms() // 실패 시 안전하게 약관 화면으로
        }
    }

    /**
     * 약관 동의 화면으로 이동
     */
    private fun navigateToTerms() {
        navigate("terms")
    }

    /**
     * 프로필 설정 화면으로 이동
     */
    private fun navigateToProfile() {
        navigate("profile")
    }

    /**
     * 홈 화면으로 이동
     */
    private fun navigateToHome() {
        navigate("home")
    }

    /**
     * 실제 네비게이션 수행
     */
    private fun navigate(destination: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", destination)
            // 백스택 정리
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}