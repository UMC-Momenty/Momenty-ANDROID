package com.example.momenty.domain.calendar

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import java.util.Calendar

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val dayText: TextView = itemView.findViewById(R.id.tv_day)
        private val eventIndicatorLayout: LinearLayout = itemView.findViewById(R.id.ll_event_indicators)
        private val eventCountText: TextView = itemView.findViewById(R.id.tv_event_count)
        private val dayContainer: View = itemView.findViewById(R.id.day_container)

        fun bind(day: CalendarDay){
            dayText.text = if(day.isCurrentMonth) day.day.toString() else ""

            // 오늘 날짜 표시
            if(day.isToday) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_selected)
                dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                dayText.typeface = Typeface.DEFAULT_BOLD
            }
            // 선택된 날짜
            else if (day.isSelected) {
                dayContainer.setBackgroundResource(R.drawable.bg_calendar_selected)
                dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                dayText.typeface = Typeface.DEFAULT_BOLD
            }
            // 일반 날짜
            else {
                dayContainer.background = null
                dayText.setTextColor(
                    if (day.isCurrentMonth)
                        ContextCompat.getColor(itemView.context, R.color.body_1)
                    else
                        ContextCompat.getColor(itemView.context, R.color.caption_1)
                )
                dayText.typeface = Typeface.DEFAULT
            }

            // 일정 인디케이터 표시
            eventIndicatorLayout.removeAllViews()
            eventCountText.visibility = View.GONE

            if (day.events.isNotEmpty() && day.isCurrentMonth) {
                val eventCount = day.events.size

                if (eventCount <= 3) {
                    // 최대 3개까지 점으로 표시
                    day.events.take(3).forEach { event ->
                        val dot = View(itemView.context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(5), dpToPx(5)
                            ).apply {
                                marginEnd = dpToPx(2)
                            }
                            setBackgroundResource(R.drawable.bg_event_dot)
                            // 일정 색상 적용
                            event.color?.let {
                                setBackgroundColor(it)
                            }
                        }
                        eventIndicatorLayout.addView(dot)
                    }
                } else {
                    // 3개 초과 시 (+개수) 텍스트로 표시
                    eventCountText.visibility = View.VISIBLE
                    eventCountText.text = "+${eventCount}"
                }
            }

            // 클릭 리스너
            itemView.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClick(day)
                }
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }
}