package com.example.momenty.domain.chatbot

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentChatbotRoomBinding
import com.example.momenty.domain.chatbot.adapter.ChatMessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatbotRoomFragment : Fragment(R.layout.fragment_chatbot_room) {

    private var _binding: FragmentChatbotRoomBinding? = null
    private val binding get() = _binding!!

    private var baseTopBarHeight = 0
    private var baseBottomInputPaddingBottom = 0
    private var baseRvPaddingBottom = 0

    private var consumedFirstMessage = false
    private val viewModel: ChatbotRoomViewModel by viewModels()
    private val messageAdapter = ChatMessageAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatbotRoomBinding.bind(view)

        baseTopBarHeight = binding.topBar.layoutParams.height
        baseBottomInputPaddingBottom = binding.bottomInput.paddingBottom
        baseRvPaddingBottom = binding.rvMessages.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottom = maxOf(imeBottom, navBottom)

            binding.topBar.updatePadding(top = statusTop)
            binding.topBar.layoutParams = binding.topBar.layoutParams.apply {
                height = baseTopBarHeight + statusTop
            }

            binding.bottomInput.updatePadding(
                bottom = baseBottomInputPaddingBottom +
                        bottom +
                        resources.getDimensionPixelSize(R.dimen.chat_input_extra_bottom)
            )

            binding.rvMessages.updatePadding(
                bottom = baseRvPaddingBottom +
                        bottom +
                        resources.getDimensionPixelSize(R.dimen.chat_list_extra_bottom)
            )

            insets
        }


        binding.tvRoomTitle.text = "챗봇"

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack(R.id.chatbotFragment, false)
        }


        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = false
                reverseLayout = false
            }
            adapter = messageAdapter
        }

        val roomId = arguments?.getLong("roomId") ?: 0L
        val firstMessage = arguments?.getString("firstMessage").orEmpty()

        viewModel.init(roomId)

        if (!consumedFirstMessage && firstMessage.isNotBlank()) {
            consumedFirstMessage = true
            viewModel.send(firstMessage)
        }


        // + 메뉴
        binding.ivPlus.setOnClickListener { showAddMenu() }

        // 전송
        binding.ivSend.setOnClickListener {
            val text = binding.etMessage.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            viewModel.send(text)
            binding.etMessage.setText("")
        }




        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collectLatest { list ->
                messageAdapter.submitList(list)
            }
        }
    }

    private fun showAddMenu() {
        val content = layoutInflater.inflate(R.layout.popup_chat_add_menu, null)

        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            elevation = 12f
        }

        content.findViewById<View>(R.id.tvMute).setOnClickListener { popup.dismiss() }
        content.findViewById<View>(R.id.tvDelete).setOnClickListener { popup.dismiss() }


        val loc = IntArray(2)
        binding.bottomInput.getLocationOnScreen(loc)
        val bottomInputTopY = loc[1]


        content.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupH = content.measuredHeight


        val plusLoc = IntArray(2)
        binding.ivPlus.getLocationOnScreen(plusLoc)
        val x = plusLoc[0]
        val y = bottomInputTopY - popupH

        popup.showAtLocation(binding.root, android.view.Gravity.TOP or android.view.Gravity.START, x, y)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
