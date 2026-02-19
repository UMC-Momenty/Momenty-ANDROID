package com.example.momenty.domain.main.presentation

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.momenty.R
import com.example.momenty.data.repository.AuthRepository
import com.example.momenty.databinding.ActivityMainBinding
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        handleIntentIfNeeded()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val isInAuthGraph = destination.isInGraph(R.id.auth_graph)
            val hideBottomNav = arguments?.getBoolean(ARG_HIDE_BOTTOM_NAV) ?: false

            binding.bottomNav.visibility =
                if (isInAuthGraph || hideBottomNav) View.GONE else View.VISIBLE
        }
    }

    /**
     * SplashActivity에서 전달된 Intent 처리
     */
    private fun handleIntentIfNeeded() {
        val navigateTo = intent.getStringExtra("navigate_to")

        when (navigateTo) {
            "home" -> {
                // home_graph로 이동
                navController.navigate(R.id.home_graph)
                binding.bottomNav.selectedItemId = R.id.home_graph
            }

            "terms" -> {
                if (navController.currentDestination?.id != R.id.termsFragment) {
                    navController.navigate(R.id.auth_graph)
                }
            }

            "profile" -> {
                navController.navigate(R.id.auth_graph)
                navController.navigate(R.id.userProfileFragment)
            }
        }

        intent.removeExtra("navigate_to")
    }

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun logout() {
        lifecycleScope.launch {
            authRepository.logout()
            // 로그인 화면으로 이동
            navController.navigate(R.id.auth_graph)
        }
    }

    /**
     * destination이 특정 graph(또는 그 하위)에 속하는지 체크
     */
    private fun NavDestination.isInGraph(@androidx.annotation.IdRes graphId: Int): Boolean {
        var current: NavDestination? = this
        while (current != null) {
            if (current.id == graphId) return true
            current = current.parent
        }
        return false
    }

    companion object {
        const val ARG_HIDE_BOTTOM_NAV = "hideBottomNav"
    }
}
