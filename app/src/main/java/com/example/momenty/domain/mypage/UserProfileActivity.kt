package com.example.momenty.domain.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momenty.databinding.ActivityUserProfileBinding

class UserProfileActivity: AppCompatActivity() {
    lateinit var binding: ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnUserProfileBack.setOnClickListener {
            finish()
        }
    }
}