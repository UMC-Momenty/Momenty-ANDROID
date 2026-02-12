package com.example.momenty.domain.member

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginVPA(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LoginPage1Fragment()
            1 -> LoginPage2Fragment()
            2 -> LoginPage3Fragment()
            else -> LoginPage3Fragment()
        }
    }
}