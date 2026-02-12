package com.example.momenty.domain.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemAlarmCardviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                if(alarm.duration.isNullOrBlank()){
                    llDurationSection.visibility = View.GONE
                } else{
                    llDurationSection.visibility = View.VISIBLE
                    tvDuration.text = alarm.duration
                }
                // 알림 요일/날짜
                // 일회성 : 날짜, 반복성 : 요일
                tvRepeatDays.text = if(alarm.isRepeat){
                    formatRepeatDays(alarm.repeatDays)
                } else{
                    formatDate(alarm.alarmDate)
                }
                // 알림 시간
                tvAlarmTime.text = alarm.alarmTime
                // 스위치 상태
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

        private fun formatRepeatDays(repeatDays: List<Int>?): String {
            if(repeatDays.isNullOrEmpty()) return ""

            // 모든 요일이 선택된 경우 (1~7 모두 포함)
            if(repeatDays.size == 7 && repeatDays.containsAll(listOf(1,2,3,4,5,6,7))){
                return "매일"
            }

            val dayNames = listOf("월", "화", "수", "목", "금", "토", "일")
            return repeatDays.sorted().joinToString(" ") { day ->
                dayNames.getOrNull(day - 1) ?: ""
            }
        }

        private fun formatDate(date: Date): String {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
            return dateFormat.format(date)
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