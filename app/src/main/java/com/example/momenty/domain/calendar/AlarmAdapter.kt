package com.example.momenty.domain.calendar

import android.view.LayoutInflater
import android.view.View
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
    ): AlarmViewHolder {
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
                // 알람 제목
                tvAlarmTitle.text = alarm.title

                // 지속 시간 (입력한 경우에만 보임)
                if (alarm.durationMinutes == 0) {
                    llDurationSection.visibility = View.GONE
                } else {
                    llDurationSection.visibility = View.VISIBLE
                    tvDuration.text = formatDuration(alarm.durationMinutes)
                }

                // 알림 요일/날짜
                // 일회성: 날짜, 반복성: 요일
                tvRepeatDays.text = alarm.getRepeatDaysDisplay()

                // 알림 시간 (Alarm.getFormattedTime() 사용)
                tvAlarmTime.text = alarm.getFormattedTime()

                // 스위치 상태
                switchAlarm.isChecked = alarm.isAlarmEnabled

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

        /**
         * 지속 시간을 "1시간 30분" 형식으로 변환
         */
        private fun formatDuration(minutes: Int): String {
            return when {
                minutes == 0 -> ""
                minutes < 60 -> "${minutes}분"
                minutes % 60 == 0 -> "${minutes / 60}시간"
                else -> "${minutes / 60}시간 ${minutes % 60}분"
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.scheduleId == newItem.scheduleId
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }
    }
}