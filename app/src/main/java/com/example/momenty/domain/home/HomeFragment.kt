package com.example.momenty.domain.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.momenty.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDate()
        initClickListeners()
    }

    private fun initDate() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        binding.tvHomeDate.text = today.format(formatter)
    }

    private fun initClickListeners() {
        // 하단 질문 이동 버튼
        binding.ivHomeToQuestion.setOnClickListener {
            navigateToQuest()
        }

        // 질문 텍스트 영역 (연필 아이콘 포함) 클릭
        binding.tvCardQuestion.setOnClickListener {
            navigateToQuest()
        }

        // 알림 버튼
        binding.btnNotification.setOnClickListener {
            navigateToNotification()
        }
    }

    private fun navigateToQuest() {
        val intent = Intent(requireContext(), QuestActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToNotification() {
        // TODO: 알림 페이지 띄우기
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}