package com.example.momenty.domain.main.presentation


import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.momenty.R
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.momenty.databinding.ActivityMainBinding
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.bottom_nav)
            .setupWithNavController(navController)
    }
}
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        handleIntent()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        if (navHostFragment != null) {
            val navController = navHostFragment.navController
        }
    }

    /**
     * SplashActivity에서 전달된 Intent 처리
     */
    private fun handleIntent() {
        val navigateTo = intent.getStringExtra("navigate_to")

        if (navigateTo != null) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            navHostFragment?.let {
                val navController = it.navController

                when (navigateTo) {
                    "record" -> {
                        // 로그인 상태 → RecordFragment로 이동
                        // Navigation Graph의 시작 지점이 이미 설정되어 있으면 자동 이동
                    }
                    "terms" -> {
                        // 비로그인 상태 → TermsFragment로 이동
                        // Navigation Graph의 시작 지점이 이미 설정되어 있으면 자동 이동
                    }
                }
            }
        }
    }

    /**
     * 로그인 정보 저장
     * (기존 SharedPreferences 방식 유지 - UI 표시용)
     * 실제 JWT 토큰은 TokenManager에서 관리됨
     */
    fun saveLoggedIn(userId: String, userName: String) {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("user_id", userId)
            putString("user_name", userName)
            apply()
        }
    }

    /**
     * 로그인 정보 확인
     */
    fun isLoggedIn(): Boolean {
        // TokenManager의 로그인 여부 확인
        return tokenManager.isLoggedIn()
    }

    /**
     * 로그아웃
     */
    fun logout() {
        // UI용 SharedPreferences 삭제
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // JWT 토큰 삭제는 AuthRepository에서 처리
    }

    /**
     * 사용자 ID 가져오기
     */
    fun getUserId(): String? {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", null)
    }

    /**
     * 사용자 이름 가져오기
     */
    fun getUserName(): String? {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_name", null)
    }
}
