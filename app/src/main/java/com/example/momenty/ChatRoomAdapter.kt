package com.example.momenty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemChatbotRoomBinding

class ChatRoomAdapter(
    private val onChatRoomClick: (ChatRoomItem) -> Unit,
    private val onChatRoomLongClick: (ChatRoomItem) -> Unit
) : ListAdapter<ChatRoomItem, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    inner class ChatRoomViewHolder(private val binding: ItemChatbotRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoom: ChatRoomItem) {
            binding.tvChatTitle.text = chatRoom.title
            binding.tvLastMessage.text = chatRoom.lastMessage
            binding.tvTimestamp.text = chatRoom.timestamp

            binding.root.setOnClickListener { onChatRoomClick(chatRoom) }

            // 롱 클릭 시 삭제 콜백 호출
            binding.root.setOnLongClickListener {
                onChatRoomLongClick(chatRoom)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ItemChatbotRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoomItem>() {
        override fun areItemsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatRoomItem, newItem: ChatRoomItem): Boolean = oldItem == newItem
    }
}