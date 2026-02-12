package com.example.momenty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemChatbotMessageBinding
import com.example.momenty.databinding.ItemUserMessageBinding

class ChatBotMessageAdapter : ListAdapter<ChatMessageItem, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        const val VIEW_TYPE_USER = 0
        const val VIEW_TYPE_BOT = 1
    }

    inner class UserMessageViewHolder(private val binding: ItemUserMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageItem) {
            binding.tvUserMessage.text = message.text
        }
    }

    inner class BotMessageViewHolder(private val binding: ItemChatbotMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessageItem) {
            binding.tvBotMessage.text = message.text
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemUserMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            UserMessageViewHolder(binding)
        } else {
            val binding = ItemChatbotMessageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            BotMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserMessageViewHolder -> holder.bind(getItem(position))
            is BotMessageViewHolder -> holder.bind(getItem(position))
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessageItem>() {
        override fun areItemsTheSame(oldItem: ChatMessageItem, newItem: ChatMessageItem): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: ChatMessageItem, newItem: ChatMessageItem): Boolean {
            return oldItem == newItem
        }
    }
}