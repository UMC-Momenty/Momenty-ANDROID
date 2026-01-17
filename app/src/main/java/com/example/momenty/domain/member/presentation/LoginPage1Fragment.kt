package com.example.momenty.domain.member.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momenty.databinding.FragmentLoginPage1Binding

class LoginPage1Fragment : Fragment() {

    private var _binding: FragmentLoginPage1Binding? = null
    private val binding get() = _binding!!

    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginPage1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3초마다 텍스트 전환
        startTextTransition()
    }

    private fun startTextTransition() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentPage = if (currentPage == 0) 1 else 0
                animateTextChange()
                handler.postDelayed(this, 3000) // 3초마다 전환
            }
        }, 3000)
    }

    private fun animateTextChange() {
        // Fade out
        binding.tvLoginTitle.animate()
            .alpha(0f)
            .setDuration(100)
            .withEndAction {
                // 텍스트 변경
                if (currentPage == 0) {
                    binding.tvLoginTitle.text = "매일 한 개씩 오늘의 질문에 답변해요!"
                    binding.tvLoginDescription.text =
                        "매일 달라지는 질문을 통해\n반려동물의 하루를 조금 더 깊이 돌아보고,\n다양한 기록이 되고, 기록은 추억이 돼요."
                } else {
                    binding.tvLoginTitle.text = "특별한 날이 아니어도 괜찮아요."
                    binding.tvLoginDescription.text =
                        "사소했던 오늘이, 가장 소중한 기억이 되니까요.\nMomenty에서 반려동물의 하루를 기록해 보세요."
                }

                // Fade in
                binding.tvLoginTitle.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()

                binding.tvLoginDescription.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start()
            }
            .start()

        binding.tvLoginDescription.animate()
            .alpha(0f)
            .setDuration(300)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // 메모리 누수 방지
        _binding = null
    }
}