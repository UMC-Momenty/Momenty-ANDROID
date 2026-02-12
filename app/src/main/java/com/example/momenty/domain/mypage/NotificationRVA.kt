package com.example.momenty.domain.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemInquiryHistoryBinding
import com.example.momenty.databinding.ItemNotificationBinding

class NotificationRVA(private val notificationList: ArrayList<NotificationData>)
    : RecyclerView.Adapter<NotificationRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(history: NotificationData)
        }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationRVA.viewHolder {
        val binding: ItemNotificationBinding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationRVA.viewHolder, position: Int) {
        holder.bind(notificationList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(notificationList[position])
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    inner class viewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationData) {
            binding.tvNotificationTitle.text = data.title
            binding.tvNotificationEnabled.text = data.enabled
        }
    }
}