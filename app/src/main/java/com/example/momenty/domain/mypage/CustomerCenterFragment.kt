package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.databinding.FragmentCustomerCenterBinding
import com.google.android.material.tabs.TabLayoutMediator

class CustomerCenterFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterBinding
    private val tabList = arrayListOf("FAQ", "문의내역", "문의하기")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterBinding.inflate(inflater, container, false)

        initListener()
        setVPA()

        return binding.root
    }

    private fun initListener() {
        binding.ivCustomerCenterBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setVPA() {
        val CustomerCenterAdapter = CustomerCenterVPA(this)
        binding.vpCustomerCenter.adapter = CustomerCenterAdapter

        TabLayoutMediator(binding.tbCustomerCenter, binding.vpCustomerCenter) {
            tab, position ->
            tab.text = tabList[position]
        }.attach()
    }
}