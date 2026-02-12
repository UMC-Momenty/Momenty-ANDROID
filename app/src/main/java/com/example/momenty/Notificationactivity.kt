package com.example.momenty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var navigationManager: BottomNavigationManager
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTopBar()
        setupRecyclerView()
        setupBottomNavigation()
        loadNotifications()
    }

    private fun setupTopBar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            NotificationRepository.markAsRead(notification.id)
            loadNotifications()
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = this@NotificationActivity.adapter
        }
    }

    private fun setupBottomNavigation() {
        // binding.bottomNavigation.root로 View 전달
        navigationManager = BottomNavigationManager(this, binding.bottomNavigation.root)
        navigationManager.selectItem("home")

        navigationManager.setOnNavigationItemSelectedListener { selectedItem ->
            when (selectedItem) {
                "home" -> {
                    finish()
                }
                "chatbot" -> {
                    // ChatBotListActivity로 이동
                }
                else -> {}
            }
        }
    }

    private fun loadNotifications() {
        val notifications = NotificationRepository.getAllNotifications()
        adapter.submitList(notifications)
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }
}