package com.example.momenty.domain.calendar

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
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
        val diffCallback = CalendarDiffCallback(days, newDays)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        days = newDays
        diffResult.dispatchUpdatesTo(this)
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val dayText: TextView = itemView.findViewById(R.id.tv_day)
        private val eventIndicatorLayout: LinearLayout = itemView.findViewById(R.id.ll_event_indicators)
        private val eventCountText: TextView = itemView.findViewById(R.id.tv_event_count)
        private val dayContainer: View = itemView.findViewById(R.id.fl_day_container)

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

            // 일정 인디케이터 표시 - 최적화
            updateEventIndicators(day)

            // 클릭 리스너
            itemView.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClick(day)
                }
            }
        }
        private fun updateEventIndicators(day: CalendarDay) {
            // 현재 월이 아니거나 이벤트가 없으면 숨김
            if (!day.isCurrentMonth || day.events.isEmpty()) {
                eventIndicatorLayout.visibility = View.GONE
                eventCountText.visibility = View.GONE
                return
            }

            eventIndicatorLayout.visibility = View.VISIBLE
            eventIndicatorLayout.removeAllViews()
            eventCountText.visibility = View.GONE

            val eventCount = day.events.size

            if (eventCount <= 3) {
                // 최대 3개까지 점으로 표시
                day.events.take(3).forEach { event ->
                    val dot = createDotView(event.color)
                    eventIndicatorLayout.addView(dot)
                }
            } else {
                // 3개 초과 시 (+개수) 텍스트로 표시
                eventCountText.visibility = View.VISIBLE
                eventCountText.text = "+${eventCount}"
            }
        }

        private fun createDotView(color: Int?): View {
            return View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(5), dpToPx(5)
                ).apply {
                    marginEnd = dpToPx(2)
                }
                setBackgroundResource(R.drawable.bg_event_dot)
                // 일정 색상 적용
                color?.let {
                    setBackgroundColor(it)
                }
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }

    /**
     * DiffUtil Callback for efficient updates
     */
    private class CalendarDiffCallback(
        private val oldList: List<CalendarDay>,
        private val newList: List<CalendarDay>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldDay = oldList[oldItemPosition]
            val newDay = newList[newItemPosition]
            return oldDay.day == newDay.day &&
                    oldDay.month == newDay.month &&
                    oldDay.year == newDay.year
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldDay = oldList[oldItemPosition]
            val newDay = newList[newItemPosition]
            return oldDay.isSelected == newDay.isSelected &&
                    oldDay.isToday == newDay.isToday &&
                    oldDay.isCurrentMonth == newDay.isCurrentMonth &&
                    oldDay.events.size == newDay.events.size
        }
    }
}