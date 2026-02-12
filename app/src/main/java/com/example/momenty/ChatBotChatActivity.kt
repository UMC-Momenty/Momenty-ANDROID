package com.example.momenty

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.ActivityChatbotChatBinding

class ChatBotChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatbotChatBinding
    private lateinit var messageAdapter: ChatBotMessageAdapter
    private var chatRoomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: ""
        setupViews(intent.getStringExtra("CHAT_ROOM_TITLE") ?: "챗봇")
        loadMessages()
    }

    private fun setupViews(title: String) {
        binding.tvTitle.text = title
        binding.ivBack.setOnClickListener { finish() }

        binding.ivSearchTrigger.setOnClickListener {
            binding.layoutDefaultHeader.visibility = View.GONE
            binding.layoutSearchHeader.visibility = View.VISIBLE
            binding.etSearchInput.requestFocus()
        }

        binding.tvSearchCancel.setOnClickListener {
            binding.layoutSearchHeader.visibility = View.GONE
            binding.layoutDefaultHeader.visibility = View.VISIBLE
            binding.etSearchInput.setText("")
        }

        messageAdapter = ChatBotMessageAdapter()
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatBotChatActivity)
            adapter = messageAdapter
        }

        binding.ivSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
                binding.etMessage.setText("")
            }
        }

        binding.btnAttach.setOnClickListener {
            binding.attachmentOptions.visibility = if (binding.attachmentOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun sendMessage(text: String) {
        val userMsg = ChatMessageItem(System.currentTimeMillis().toString(), text, true)
        ChatBotRepository.addMessageToRoom(chatRoomId, userMsg)
        updateMessageList()

        Handler(Looper.getMainLooper()).postDelayed({
            val botMsg = ChatMessageItem(System.currentTimeMillis().toString(), "먼티가 당신의 이야기를 들었어요!", false)
            ChatBotRepository.addMessageToRoom(chatRoomId, botMsg)
            updateMessageList()
        }, 1000)
    }

    private fun updateMessageList() {
        ChatBotRepository.getChatRoom(chatRoomId)?.let {
            messageAdapter.submitList(it.messages.toList())
            if (it.messages.isNotEmpty()) binding.rvMessages.scrollToPosition(it.messages.size - 1)
        }
    }

    private fun loadMessages() { updateMessageList() }
}