package com.example.momenty.domain.main.presentation

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.momenty.R
import com.example.momenty.databinding.ActivityMainBinding
import com.example.momenty.global.security.TokenManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject





@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // 하단 바 숨길 화면들
                R.id.termsFragment,
                R.id.loginPage3Fragment,
                    R.id.recordWriteFragment
                -> {
                    binding.bottomNav.visibility = View.GONE
                }


                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }


    /**
     * SplashActivity에서 전달된 Intent 처리
     */
    private fun handleIntent() {
        val navigateTo = intent.getStringExtra("navigate_to") ?: return


        when (navigateTo) {
            "record" -> {
                // 예: navController.navigate(R.id.recordFragment)
            }
            "terms" -> {
                // 예: navController.navigate(R.id.termsFragment)
            }
        }
    }

    /**
     * 로그인 정보 저장 (UI 표시용 SharedPreferences)
     * 실제 JWT 토큰은 TokenManager에서 관리
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
        return tokenManager.isLoggedIn()
    }

    /**
     * 로그아웃
     */
    fun logout() {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        // JWT 토큰 삭제는 AuthRepository 등에서 처리
    }

    fun getUserId(): String? {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_id", null)
    }

    fun getUserName(): String? {
        val prefs = getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_name", null)
    }
}
