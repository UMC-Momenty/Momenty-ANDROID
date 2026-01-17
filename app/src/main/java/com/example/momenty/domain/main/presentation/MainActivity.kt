package com.example.momenty.domain.main.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.momenty.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val SPLASH_DURATION = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        setContentView(R.layout.activity_main)

        // PreferenceManager 초기화
        preferenceManager = PreferenceManager.getInstance(this)

        setupWindowInsets()
        setupNavigation()

        lifecycleScope.launch {
            delay(SPLASH_DURATION)
            navigateToStartScreen()
            keepSplashOnScreen = false
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun navigateToStartScreen() {
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // 사용자 상태에 따른 화면 분기
        val startDestination = when {
            // 1. 로그인 완료 → 메인 화면 (자동 로그인)
            preferenceManager.isLoggedIn -> {
                R.id.recordFragment
            }
            // 2. 로그인 안함 → 항상 약관 화면부터 시작
            else -> {
                R.id.termsFragment
            }
        }

        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    fun saveTermsAgreed() {
        // 약관 동의는 임시로 저장하지 않음
        // 로그인 완료 시에만 저장됨
        // preferenceManager.isTermsAgreed = true
    }

    fun saveLoggedIn(userId: String? = null, userName: String? = null) {
        // 로그인 성공 시 약관 동의도 함께 저장
        preferenceManager.isTermsAgreed = true
        preferenceManager.isLoggedIn = true
        userId?.let { preferenceManager.userId = it }
        userName?.let { preferenceManager.userName = it }
    }

    fun logout() {
        preferenceManager.logout()

        // 로그아웃 후 약관 화면으로 이동
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.termsFragment)
        navController.graph = navGraph
    }

    fun getNavController(): NavController = navController

    fun getPreferenceManager(): PreferenceManager = preferenceManager
}