package com.example.momenty

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.ActivityMainBinding
import com.example.momenty.databinding.ActivityNotificationBinding
import com.example.momenty.databinding.ActivityChatbotListBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationManager: BottomNavigationManager
    private var currentScreen = "home"
    private var notificationBinding: ActivityNotificationBinding? = null
    private var notificationAdapter: NotificationAdapter? = null
    private var chatbotBinding: ActivityChatbotListBinding? = null
    private var chatRoomAdapter: ChatRoomAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackPressHandler()
        setupNavigation()
        setupListeners()
        showHomeScreen()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen != "home") {
                    showHomeScreen()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupNavigation() {
        navigationManager = BottomNavigationManager(this, binding.bottomNavigationMenu.root)
        navigationManager.selectItem("home")
        navigationManager.setOnNavigationItemSelectedListener {
            when (it) {
                "chatbot" -> showChatBotScreen()
                "home" -> showHomeScreen()
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.btnNotification.setOnClickListener { showNotificationScreen() }
        binding.btnPetSelection.setOnClickListener { showPetSelectionDialog() }
        binding.cardQuestion.setOnClickListener { handleStateTransition() }
        binding.btnWriteMoment.setOnClickListener { handleStateTransition() }
    }

    private fun showPetSelectionDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
            setBackgroundColor(android.graphics.Color.WHITE)
        }
        val titleLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        val titleText = TextView(this).apply {
            text = "반려동물 선택"
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val petIcon = ImageView(this).apply {
            setImageResource(R.drawable.cheesecat_action1)
            layoutParams = LinearLayout.LayoutParams(120, 120)
        }
        titleLayout.addView(titleText)
        titleLayout.addView(petIcon)

        dialogView.addView(titleLayout)
        dialogView.addView(createPetItem("먼티", true) { dialog.dismiss() })
        dialogView.addView(createPetItem("닉네임", false) { dialog.dismiss() })

        dialog.setContentView(dialogView)
        dialog.window?.apply {
            setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        dialog.show()
    }

    private fun createPetItem(name: String, isSelected: Boolean, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 20 }
            val icon = ImageView(this@MainActivity).apply {
                setImageResource(R.drawable.ic_top_sole_orange)
                layoutParams = LinearLayout.LayoutParams(100, 100)
            }
            val nameText = TextView(this@MainActivity).apply {
                text = name
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = 30 }
            }
            val checkIcon = ImageView(this@MainActivity).apply {
                setImageResource(android.R.drawable.checkbox_on_background)
                layoutParams = LinearLayout.LayoutParams(60, 60)
                visibility = if (isSelected) View.VISIBLE else View.GONE
            }
            addView(icon)
            addView(nameText)
            addView(checkIcon)
            setOnClickListener { onClick() }
        }
    }

    private fun showHomeScreen() {
        currentScreen = "home"
        notificationBinding?.root?.visibility = View.GONE
        chatbotBinding?.root?.visibility = View.GONE
        binding.statusBar.visibility = View.VISIBLE
        binding.topArea.visibility = View.VISIBLE
        binding.cardQuestion.visibility = View.VISIBLE
        binding.characterArea.visibility = View.VISIBLE
        binding.bottomNavigationMenu.root.visibility = View.VISIBLE
        navigationManager.selectItem("home")
        updateUI()
    }

    private fun showNotificationScreen() {
        currentScreen = "notification"
        if (notificationBinding == null) {
            notificationBinding = ActivityNotificationBinding.inflate(LayoutInflater.from(this))
            notificationBinding?.root?.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            binding.root.addView(notificationBinding?.root)
            notificationBinding?.btnBack?.setOnClickListener { showHomeScreen() }
            notificationAdapter = NotificationAdapter {
                NotificationRepository.markAsRead(it.id)
                loadNotifications()
            }
            notificationBinding?.rvNotifications?.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = notificationAdapter
            }
            notificationBinding?.bottomNavigation?.root?.visibility = View.GONE
        }
        binding.statusBar.visibility = View.GONE
        binding.topArea.visibility = View.GONE
        binding.cardQuestion.visibility = View.GONE
        binding.characterArea.visibility = View.GONE
        binding.bottomNavigationMenu.root.visibility = View.GONE
        notificationBinding?.root?.visibility = View.VISIBLE
        chatbotBinding?.root?.visibility = View.GONE
        loadNotifications()
    }

    private fun showChatBotScreen() {
        currentScreen = "chatbot"
        if (chatbotBinding == null) {
            chatbotBinding = ActivityChatbotListBinding.inflate(LayoutInflater.from(this))
            chatbotBinding?.root?.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
            binding.root.addView(chatbotBinding?.root)
            chatbotBinding?.ivBack?.setOnClickListener { showHomeScreen() }

            chatRoomAdapter = ChatRoomAdapter(
                onChatRoomClick = { room -> moveToChatRoom(room.id, room.title) },
                onChatRoomLongClick = { room ->
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("대화 삭제")
                        .setMessage("'${room.title}' 대화를 삭제하시겠습니까?")
                        .setPositiveButton("삭제") { _, _ ->
                            ChatBotRepository.deleteChatRoom(room.id)
                            loadChatRooms()
                        }
                        .setNegativeButton("취소", null)
                        .show()
                }
            )
            chatbotBinding?.rvChatRooms?.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = chatRoomAdapter
            }

            chatbotBinding?.inputArea?.setOnClickListener {
                val newRoom = ChatBotRepository.createNewChatRoom("새로운 대화")
                moveToChatRoom(newRoom.id, newRoom.title)
            }
            chatbotBinding?.bottomNavigation?.root?.visibility = View.GONE
        }

        binding.statusBar.visibility = View.GONE
        binding.topArea.visibility = View.GONE
        binding.cardQuestion.visibility = View.GONE
        binding.characterArea.visibility = View.GONE
        binding.bottomNavigationMenu.root.visibility = View.GONE
        chatbotBinding?.root?.visibility = View.VISIBLE
        notificationBinding?.root?.visibility = View.GONE
        loadChatRooms()
        navigationManager.selectItem("chatbot")
    }

    private fun moveToChatRoom(id: String, title: String) {
        val intent = Intent(this, ChatBotChatActivity::class.java)
        intent.putExtra("CHAT_ROOM_ID", id)
        intent.putExtra("CHAT_ROOM_TITLE", title)
        startActivity(intent)
    }

    private fun handleStateTransition() {
        with(MomentRepository) {
            when (getTodayMoment().state) {
                MomentState.EMPTY -> updateMomentState(MomentState.QUESTION_WRITING)
                MomentState.QUESTION_WRITING -> {
                    updateMomentState(MomentState.QUESTION_COMPLETE)
                    updateQuestion("질문 작성 완료!")
                    NotificationRepository.removeMissedDateNotification(getCurrentDate())
                }
                MomentState.QUESTION_COMPLETE -> updateMomentState(MomentState.ANSWER_WRITING)
                MomentState.ANSWER_WRITING -> updateMomentState(MomentState.ALL_COMPLETE)
                MomentState.ALL_COMPLETE -> updateMomentState(MomentState.EMPTY)
            }
        }
        updateUI()
    }

    private fun updateUI() {
        if (currentScreen != "home") return
        val moment = MomentRepository.getTodayMoment()
        binding.tvDate.text = moment.date
        binding.tvMomentCount.text = "쌓인 모먼티 ${MomentRepository.momentCount}개"

        // 초기 질문 텍스트 설정
        if (moment.state == MomentState.EMPTY) {
            binding.tvQuestion.text = "오늘 먼티의 기분은 어땠나요?"
        } else {
            binding.tvQuestion.text = moment.question
        }
    }

    private fun loadNotifications() {
        notificationAdapter?.submitList(NotificationRepository.getAllNotifications())
    }

    private fun loadChatRooms() {
        val chatRooms = ChatBotRepository.getAllChatRooms()
        chatRoomAdapter?.submitList(chatRooms)
        chatbotBinding?.inputArea?.visibility = if (chatRooms.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (currentScreen == "chatbot") loadChatRooms()
    }
}