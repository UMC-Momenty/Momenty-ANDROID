package com.example.momenty

object ChatBotRepository {
    private val chatRooms = mutableListOf<ChatRoomItem>()

    fun getAllChatRooms(): List<ChatRoomItem> {
        return chatRooms.toList()
    }

    fun getChatRoom(id: String): ChatRoomItem? {
        return chatRooms.find { it.id == id }
    }

    // 대화방 삭제 기능
    fun deleteChatRoom(id: String) {
        chatRooms.removeAll { it.id == id }
    }

    // 새로운 대화방 생성
    fun createNewChatRoom(firstQuestion: String): ChatRoomItem {
        val newRoom = ChatRoomItem(
            id = System.currentTimeMillis().toString(),
            title = firstQuestion.take(50), // 첫 질문을 제목으로 사용
            lastMessage = "",
            timestamp = "방금"
        )
        chatRooms.add(0, newRoom)
        return newRoom
    }

    // 메시지 추가 및 마지막 메시지 업데이트
    fun addMessageToRoom(roomId: String, message: ChatMessageItem) {
        val room = chatRooms.find { it.id == roomId }
        room?.messages?.add(message)

        val index = chatRooms.indexOfFirst { it.id == roomId }
        if (index != -1) {
            chatRooms[index] = chatRooms[index].copy(
                lastMessage = message.text.take(50),
                timestamp = "방금"
            )
        }
    }

    // 대화 내용 검색
    fun searchChatRooms(query: String): List<ChatRoomItem> {
        return chatRooms.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.lastMessage.contains(query, ignoreCase = true)
        }
    }
}