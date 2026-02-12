package com.example.momenty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.ActivityChatbotListBinding // 필요한 바인딩 확인

// [수정] 클래스 이름을 ChatBotSearchActivity로 변경하여 중복 선언 오류 해결
class ChatBotSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatbotListBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        loadChatRooms()
    }

    private fun setupViews() {
        binding.ivBack.setOnClickListener { finish() }

        // 어댑터 생성 시 클릭과 롱 클릭(삭제) 인자를 명시적으로 전달
        chatRoomAdapter = ChatRoomAdapter(
            onChatRoomClick = { chatRoom ->
                val intent = Intent(this, ChatBotChatActivity::class.java)
                intent.putExtra("CHAT_ROOM_ID", chatRoom.id)
                intent.putExtra("CHAT_ROOM_TITLE", chatRoom.title)
                startActivity(intent)
                finish()
            },
            onChatRoomLongClick = { chatRoom ->
                // 검색 화면에서도 삭제 기능이 필요하다면 추가, 아니면 비워둠
            }
        )

        binding.rvChatRooms.apply {
            layoutManager = LinearLayoutManager(this@ChatBotSearchActivity)
            adapter = chatRoomAdapter
        }
    }

    private fun loadChatRooms() {
        val chatRooms = ChatBotRepository.getAllChatRooms()
        chatRoomAdapter.submitList(chatRooms)
    }
}