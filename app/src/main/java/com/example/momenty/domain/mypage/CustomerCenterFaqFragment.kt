package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momenty.databinding.FragmentCustomerCenterFaqBinding

class CustomerCenterFaqFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterFaqBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterFaqBinding.inflate(inflater, container, false)

        return binding.root
    }
}