package com.example.momenty.domain.notification

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import com.example.momenty.databinding.ItemNotification2Binding

class NotificationAdapter(
    private val onClick: (NotificationUiModel) -> Unit
) : ListAdapter<NotificationUiModel, NotificationAdapter.VH>(diff) {

    companion object {
        private val diff = object : DiffUtil.ItemCallback<NotificationUiModel>() {
            override fun areItemsTheSame(oldItem: NotificationUiModel, newItem: NotificationUiModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: NotificationUiModel, newItem: NotificationUiModel) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNotification2Binding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemNotification2Binding,
        private val onClick: (NotificationUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationUiModel) {
            binding.tvMessage.text = item.message
            binding.tvTime.text = item.timeAgo


            val ctx = binding.root.context
            val bgColor = if (!item.isRead) {
                ContextCompat.getColor(ctx, R.color.primary_sub) // 안읽음: primary
            } else {
                ContextCompat.getColor(ctx, android.R.color.white) // 읽음: white (원하면 neutral로)
            }
            binding.rootItem.setBackgroundColor(bgColor)


            val (iconRes, circleColorRes) = when (item.type) {
                NotificationType.HOME -> R.drawable.ic_notification_home to R.color.primary
                NotificationType.MYPAGE -> R.drawable.ic_notification_mypage to R.color.primary
                NotificationType.RECORD -> R.drawable.ic_notification_mypage to R.color.primary
                NotificationType.CHATBOT -> R.drawable.ic_notification_mypage to R.color.primary
                NotificationType.SYSTEM -> R.drawable.ic_notification_mypage to R.color.primary
            }

            binding.ivIcon.setImageResource(iconRes)

            // 원형 배경 tint로 색 바꾸기
            val circleColor = ContextCompat.getColor(ctx, circleColorRes)
            binding.ivIconBackground.imageTintList = ColorStateList.valueOf(circleColor)

            binding.root.setOnClickListener { onClick(item) }
        }
    }
}