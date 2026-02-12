package com.example.momenty

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.ActivityChatbotListBinding

class ChatBotListActivity : AppCompatActivity() {
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

        // 상단 돋보기 클릭 시 검색 화면으로 이동
        binding.ivSearch.setOnClickListener {
            startActivity(Intent(this, ChatBotSearchActivity::class.java))
        }

        // 1. 기존 대화 목록 어댑터 설정
        // 클릭 시 이동(onChatRoomClick)과 롱 클릭 시 삭제(onChatRoomLongClick) 인자를 모두 전달합니다.
        chatRoomAdapter = ChatRoomAdapter(
            onChatRoomClick = { chatRoom ->
                moveToChatRoom(chatRoom.id, chatRoom.title)
            },
            onChatRoomLongClick = { chatRoom ->
                // 삭제 확인 다이얼로그
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("대화 삭제")
                    .setMessage("'${chatRoom.title}' 대화를 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        ChatBotRepository.deleteChatRoom(chatRoom.id) // 리포지토리에서 삭제 실행
                        loadChatRooms() // 삭제 후 목록 새로고침
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        )

        binding.rvChatRooms.apply {
            layoutManager = LinearLayoutManager(this@ChatBotListActivity)
            adapter = chatRoomAdapter
        }

        // 2. '새로운 대화' 이미지 영역 클릭 시 새 채팅방 생성 및 이동
        binding.inputArea.setOnClickListener {
            val newRoom = ChatBotRepository.createNewChatRoom("새로운 대화")
            moveToChatRoom(newRoom.id, newRoom.title)
        }
    }

    // 공통 이동 함수: Intent를 사용하여 ChatBotChatActivity로 이동합니다.
    private fun moveToChatRoom(id: String, title: String) {
        val intent = Intent(this, ChatBotChatActivity::class.java)
        intent.putExtra("CHAT_ROOM_ID", id)
        intent.putExtra("CHAT_ROOM_TITLE", title)
        startActivity(intent)
    }

    private fun loadChatRooms() {
        val chatRooms = ChatBotRepository.getAllChatRooms()
        chatRoomAdapter.submitList(chatRooms)

        // 채팅방 목록 유무에 따라 '새로운 대화' 유도 영역(이미지뷰) 표시
        binding.inputArea.visibility = if (chatRooms.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadChatRooms() // 다른 화면에서 돌아왔을 때 목록을 최신 상태로 갱신합니다.
    }
}