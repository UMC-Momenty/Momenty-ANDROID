package com.example.momenty.domain.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemAlarmCardviewBinding

class AlarmAdapter(
    private val onToggleChanged: (Alarm, Boolean) -> Unit,
    private val onAlarmClick: (Alarm) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmAdapter.AlarmViewHolder {
        val binding = ItemAlarmCardviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return AlarmViewHolder(binding, onToggleChanged, onAlarmClick)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlarmViewHolder(
        private val binding: ItemAlarmCardviewBinding,
        private val onToggleChanged: (Alarm, Boolean) -> Unit,
        private val onAlarmClick: (Alarm) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: Alarm) {
            binding.apply {
                // 알람 정보 설정
                tvAlarmTitle.text = alarm.title
                tvDuration.text = alarm.duration
                tvRepeatDays.text = alarm.repeatDays
                tvAlarmTime.text = alarm.time
                switchAlarm.isChecked = alarm.isEnabled

                // 토글 스위치 리스너
                switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                    onToggleChanged(alarm, isChecked)
                }

                // 카드뷰 클릭 리스너
                root.setOnClickListener {
                    onAlarmClick(alarm)
                }
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }
    }
}