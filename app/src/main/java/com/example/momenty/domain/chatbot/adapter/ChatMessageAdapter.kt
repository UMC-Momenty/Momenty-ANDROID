package com.example.momenty.domain.chatbot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemChatBubbleBotBinding
import com.example.momenty.databinding.ItemChatBubbleUserBinding
import com.example.momenty.domain.chatbot.model.ChatMessage
import com.example.momenty.domain.chatbot.model.Sender

class ChatMessageAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(diff) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sender == Sender.USER) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val binding = ItemChatBubbleUserBinding.inflate(inflater, parent, false)
            UserVH(binding)
        } else {
            val binding = ItemChatBubbleBotBinding.inflate(inflater, parent, false)
            BotVH(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is UserVH -> holder.bind(item)
            is BotVH -> holder.bind(item)
        }
    }

    class UserVH(private val binding: ItemChatBubbleUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.tvMessage.text = item.text
            val max = (binding.root.resources.displayMetrics.widthPixels * 0.75f).toInt()
            binding.tvMessage.maxWidth = max
        }
    }

    class BotVH(private val binding: ItemChatBubbleBotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            binding.tvMessage.text = item.text
            val max = (binding.root.resources.displayMetrics.widthPixels * 0.75f).toInt()
            binding.tvMessage.maxWidth = max
        }
    }

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2

        private val diff = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) = oldItem == newItem
        }
    }
}
