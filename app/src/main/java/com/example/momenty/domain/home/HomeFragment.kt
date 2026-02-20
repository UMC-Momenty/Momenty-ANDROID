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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.momenty.databinding.DialogWelcomeBinding
import com.example.momenty.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private val viewModel: HomeViewModel by viewModels()

    // 회원가입 직후인지 여부 (NavArgs 또는 Arguments로 전달받음)
    private val isNewUser: Boolean by lazy {
        arguments?.getBoolean("isNewUser", false) ?: false
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
        if (isNewUser) {
            viewModel.loadMyPets()
            observeAndShowWelcomePopup()
        }
    }

    private fun observeAndShowWelcomePopup() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.petProfileImageUrl.collect { imageUrl ->
                showWelcomePopup(imageUrl)
                return@collect // 한 번만 실행
            }
        }
    }

    private fun showWelcomePopup(imageUrl: String?) {
        val popupBinding = DialogWelcomeBinding.inflate(layoutInflater, binding.root as ViewGroup, true)

        // 반려동물 프로필 이미지 로드
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(popupBinding.ivPetProfile)
        }

        // 배경 클릭 시 팝업 닫기
        popupBinding.flPopupBackground.setOnClickListener {
            popupBinding.root.visibility = View.GONE
        }

        // 3초 후 자동으로 닫기
        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000)
            if (_binding != null) {
                popupBinding.root.visibility = View.GONE
            }
        }
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