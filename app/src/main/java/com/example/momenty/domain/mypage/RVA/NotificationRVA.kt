package com.example.momenty.domain.mypage.RVA

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemNotificationBinding
import com.example.momenty.domain.home.MyNotifyInterface
import com.example.momenty.domain.home.NotificationConfirmDialog
import com.example.momenty.domain.mypage.data.NotificationData

class NotificationRVA(
    private val notificationList: ArrayList<NotificationData>,
    private val onNotificationOff: (NotificationData) -> Unit
) : RecyclerView.Adapter<NotificationRVA.viewHolder>(), MyNotifyInterface {

    lateinit var spf: SharedPreferences
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding: ItemNotificationBinding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(notificationList[position])
        holder.binding.layoutNotificationEnabled.setOnClickListener {
            val confirmDialog = NotificationConfirmDialog(this, notificationList[position], position)
            val manager = (holder.itemView.context as? FragmentActivity)?.supportFragmentManager
            confirmDialog.show(manager!!, "ConfirmDialog")
        }
        spf = holder.itemView.context.getSharedPreferences("notifySpf", android.content.Context.MODE_PRIVATE)
        spf.edit().apply {
            putString("notifyAll", "ON")
            putString("notifyMarketing", "OFF")

            apply()
        }
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onSaveClickListener(id: Int) {
        spf.getString("notifyAll", "ON")

        val currentData = notificationList[id]
        currentData.enabled = "ON"
        notifyItemChanged(id)
        onNotificationOff(currentData)
    }

    override fun onCancelClickListener(id: Int) {
        val currentData = notificationList[id]
        currentData.enabled = "OFF"
        notifyItemChanged(id)

        onNotificationOff(currentData)
    }

    inner class viewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NotificationData) {
            binding.tvNotificationTitle.text = data.title
            binding.tvNotificationEnabled.text = data.enabled

            binding.root.setOnClickListener {

            }
        }
    }
}