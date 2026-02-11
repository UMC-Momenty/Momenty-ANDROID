package com.example.momenty.domain.mypage

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CustomerCenterVPA(fragment: Fragment): FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CustomerCenterFaqFragment()
            1 -> CustomerCenterInquiryHistoryFragment()
            else -> CustomerCenterInquiryWriteFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}