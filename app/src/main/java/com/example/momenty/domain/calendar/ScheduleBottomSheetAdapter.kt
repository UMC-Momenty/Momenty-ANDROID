package com.example.momenty.domain.calendar

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import com.example.momenty.databinding.ItemBottomSheetScheduleBinding

class ScheduleBottomSheetAdapter(
    private var schedules: List<CalendarEvent>
) : RecyclerView.Adapter<ScheduleBottomSheetAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(private val binding: ItemBottomSheetScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: CalendarEvent) {
            // 일정 타입 설정 (예: 건강, 산책 등)
            binding.tvScheduleType.text = schedule.type

            // 일정 제목
            binding.tvScheduleTitle.text = schedule.title

            // 일정 시간
            binding.tvScheduleTime.text = schedule.alarmTime

            // 타입별 배경색
            applyTypeColor(schedule.type)
        }

        /**
         * 일정 타입에 따른 배경색 적용
         * backgroundTintList를 사용하여 기존 drawable의 색상만 변경
         */
        private fun applyTypeColor(type: String) {
            val colorRes = when(type) {
                "산책" -> R.color.alarm_walk
                "식사" -> R.color.alarm_eat
                "미용" -> R.color.alarm_beauty
                "건강" -> R.color.alarm_health
                "투약" -> R.color.alarm_medician
                "간식" -> R.color.alarm_treat
                "기타" -> R.color.alarm_etc
                else ->  R.color.primary
            }

            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.tvScheduleType.backgroundTintList = ColorStateList.valueOf(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemBottomSheetScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int = schedules.size

    fun updateSchedules(newSchedules: List<CalendarEvent>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }
}