package com.example.momenty.domain.chatbot

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentChatbotBinding
import com.example.momenty.domain.chatbot.adapter.ChatRoomListAdapter
import com.example.momenty.domain.chatbot.model.ChatRoom

class ChatbotFragment : Fragment(R.layout.fragment_chatbot) {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private var bottomBasePadding = 0
    private var topBarBaseHeight = 0

    private lateinit var adapter: ChatRoomListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatbotBinding.bind(view)


        bottomBasePadding = binding.bottomInput.paddingBottom
        topBarBaseHeight = binding.topBar.layoutParams.height

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            binding.topBar.updatePadding(top = statusTop)
            binding.topBar.layoutParams = binding.topBar.layoutParams.apply {
                height = topBarBaseHeight + statusTop
            }

            binding.bottomInput.updatePadding(bottom = bottomBasePadding + navBottom)

            insets
        }


        adapter = ChatRoomListAdapter { room ->
            findNavController().navigate(
                R.id.action_chatbotFragment_to_chatbotRoomFragment,
                bundleOf(
                    "roomId" to room.id,
                    "roomTitle" to room.title
                )
            )
        }


        binding.ivBack.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }



        binding.rvRooms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRooms.adapter = adapter

        // 일단 더미 목록 (UI 확인용)
        adapter.submitList(
            listOf(
                ChatRoom(1, "오늘은 어떤 하루였나요", "대화 미리보기...", "15:13"),
                ChatRoom(2, "기록 작성 도와줘", "대화 미리보기...", "14:01"),
                ChatRoom(3, "서비스 이용 안내", "대화 미리보기...", "11:22"),
            )
        )

        binding.etSearch.setOnEditorActionListener { v, _, _ ->
            val title = v.text?.toString()?.trim().orEmpty()
            if (title.isNotBlank()) {
                findNavController().navigate(
                    R.id.action_chatbotFragment_to_chatbotRoomFragment,
                    bundleOf(
                        "roomId" to 0L,
                        "roomTitle" to title
                    )
                )
            }
            true
        }

        binding.ivSend.setOnClickListener {
            val text = binding.etSearch.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            findNavController().navigate(
                R.id.action_chatbotFragment_to_chatbotRoomFragment,
                bundleOf(
                    "roomId" to 0L,
                    "roomTitle" to "챗봇",
                    "firstMessage" to text
                )
            )
            binding.etSearch.setText("")
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
