package com.example.momenty.domain.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.repository.ChatRepository
import com.example.momenty.domain.chatbot.model.ChatMessage
import com.example.momenty.domain.chatbot.model.Sender
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class ChatbotRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val idGen = AtomicLong(1)
    private var conversationId: Long = 0L

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun init(roomId: Long) {
        conversationId = roomId
        if (_messages.value.isEmpty()) {
            _messages.value = listOf(
                ChatMessage(idGen.getAndIncrement(), Sender.BOT, "안녕하세요, 모먼티입니다")
            )
        }
    }

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank()) return

        // 1) 유저 메시지 즉시 표시
        _messages.value = _messages.value + ChatMessage(
            id = idGen.getAndIncrement(),
            sender = Sender.USER,
            text = msg
        )

        // 2) 서버 호출
        viewModelScope.launch {
            runCatching {
                if (conversationId == 0L) {
                    chatRepository.requestFirst(msg).also { res ->
                        conversationId = res.chatId
                    }
                } else {
                    chatRepository.requestConversation(conversationId, msg)
                }
            }.onSuccess { res ->
                _messages.value = _messages.value + ChatMessage(
                    idGen.getAndIncrement(),
                    Sender.BOT,
                    res.answer
                )
            }.onFailure { e ->
                _messages.value = _messages.value + ChatMessage(
                    idGen.getAndIncrement(),
                    Sender.BOT,
                    "에러가 발생했어요 \n${e.message}"
                )
            }
        }
    }
}
