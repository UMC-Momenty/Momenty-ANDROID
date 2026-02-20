package com.example.momenty.domain.main.presentation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
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

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var authRepository: AuthRepository

    private lateinit var navController: NavController


    private var syncingBottomNav = false

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

        binding.bottomNav.setOnItemSelectedListener { item ->

            if (syncingBottomNav) return@setOnItemSelectedListener true

            val options = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.findStartDestination().id, false)
                .build()

            return@setOnItemSelectedListener try {
                navController.navigate(item.itemId, null, options)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        binding.bottomNav.setOnItemReselectedListener { item ->
            val graph = navController.graph.findNode(item.itemId) as? androidx.navigation.NavGraph
                ?: return@setOnItemReselectedListener


            navController.popBackStack(graph.findStartDestination().id, false)
        }


        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val isInAuthGraph = destination.isInGraph(R.id.auth_graph)
            val hideBottomNav = arguments?.getBoolean(ARG_HIDE_BOTTOM_NAV) ?: false

            binding.bottomNav.visibility =
                if (isInAuthGraph || hideBottomNav) View.GONE else View.VISIBLE


            if (!isInAuthGraph && !hideBottomNav) {
                val shouldSelect = getBottomGraphId(destination)

                if (binding.bottomNav.selectedItemId != shouldSelect) {
                    syncingBottomNav = true
                    binding.bottomNav.selectedItemId = shouldSelect
                    syncingBottomNav = false
                }
            }
        }
    }

    private fun handleIntentIfNeeded() {
        val navigateTo = intent.getStringExtra("navigate_to")

        when (navigateTo) {
            "home" -> {
                navController.navigate(R.id.home_graph)

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
            navController.navigate(R.id.auth_graph)
        }
    }

    private fun NavDestination.isInGraph(@androidx.annotation.IdRes graphId: Int): Boolean {
        var current: NavDestination? = this
        while (current != null) {
            if (current.id == graphId) return true
            current = current.parent
        }
        return false
    }


    private fun getBottomGraphId(destination: NavDestination): Int {
        return when {
            destination.isInGraph(R.id.home_graph) -> R.id.home_graph
            destination.isInGraph(R.id.record_graph) -> R.id.record_graph
            destination.isInGraph(R.id.calendar_graph) -> R.id.calendar_graph
            destination.isInGraph(R.id.community_graph) -> R.id.community_graph
            destination.isInGraph(R.id.mypage_graph) -> R.id.mypage_graph
            else -> binding.bottomNav.selectedItemId
        }
    }

    companion object {
        const val ARG_HIDE_BOTTOM_NAV = "hideBottomNav"
    }
}
