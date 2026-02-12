package com.example.momenty.domain.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.momenty.databinding.ActivityPetFormAddBinding

class PetFormAddActivity: AppCompatActivity() {
    lateinit var binding: ActivityPetFormAddBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPetFormAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnPetFormAddBack.setOnClickListener {
            finish()
        }
    }
}