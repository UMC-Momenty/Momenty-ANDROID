package com.example.momenty.domain.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.momenty.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.getValue

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val TAG = "HomeFrag"


    private val questViewModel: QuestViewModel by activityViewModels {
        val repo = QuestRepository(
            service = QuestRetrofitClient.questService
        )
        QuestViewModelFactory(repo)
    }

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

        observeQuest()
        performLoadQuest()
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

    private fun performLoadQuest() {
        questViewModel.loadQuest()
    }

    private fun observeQuest() {
        questViewModel.loadQuestResult.observe(viewLifecycleOwner) {result ->
            result.onSuccess { data ->
                Log.d(TAG, "로드 데이터: $data")
                binding.tvCardQuestion.text = data.quest
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Log.d(TAG, "질문 로드 실패: $message")
            }
        }
    }
}