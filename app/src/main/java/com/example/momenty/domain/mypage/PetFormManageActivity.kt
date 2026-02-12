package com.example.momenty.domain.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momenty.databinding.ActivityPetFormManageBinding

class PetFormManageActivity: AppCompatActivity() {
    lateinit var binding: ActivityPetFormManageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPetFormManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnPetFormManageBack.setOnClickListener {
            finish()
        }
    }
}