package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.momenty.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: LoginVPA
    private val indicators by lazy {
        listOf(
            binding.indicator1,
            binding.indicator2,
            binding.indicator3
        )
    }

    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val currentItem = binding.vpLogin.currentItem
            val nextItem = if (currentItem < 2) currentItem + 1 else 0

            binding.vpLogin.setCurrentItem(nextItem, true)

            // 5초 후 다시 실행
            autoScrollHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupIndicators()
        startAutoScroll()
    }

    private fun setupViewPager() {
        viewPagerAdapter = LoginVPA(this)
        binding.vpLogin.adapter = viewPagerAdapter

        binding.vpLogin.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        // 사용자가 드래그 시작하면 자동 스크롤 중지
                        stopAutoScroll()
                    }
                    ViewPager2.SCROLL_STATE_IDLE -> {
                        // 스크롤이 멈추면 다시 자동 스크롤 시작
                        startAutoScroll()
                    }
                }
            }
        })
    }

    private fun setupIndicators() {
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        indicators.forEachIndexed { index, indicator ->
            indicator.isSelected = (index == position)
        }
    }

    private fun startAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
        _binding = null
    }
}