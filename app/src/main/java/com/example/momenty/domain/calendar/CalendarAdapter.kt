package com.example.momenty.domain.calendar

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R

/**
 * 캘린더 어댑터 - ANR 방지를 위해 AsyncListDiffer 사용
 */
class CalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    // AsyncListDiffer를 사용하여 백그라운드에서 DiffUtil 계산
    private val differ = AsyncListDiffer(this, DiffCallback())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    /**
     * 백그라운드 스레드에서 diff 계산하여 ANR 방지
     */
    fun updateDays(newDays: List<CalendarDay>) {
        differ.submitList(newDays)
    }

    /**
     * 현재 표시 중인 날짜 목록
     */
    fun getCurrentList(): List<CalendarDay> = differ.currentList

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.tv_day)
        private val eventIndicatorLayout: LinearLayout = itemView.findViewById(R.id.ll_event_indicators)
        private val eventCountText: TextView = itemView.findViewById(R.id.tv_event_count)
        private val dayContainer: View = itemView.findViewById(R.id.fl_day_container)

        fun bind(day: CalendarDay) {
            // 현재 월이 아닌 날짜는 빈 텍스트 표시
            dayText.text = if (day.isCurrentMonth) day.day.toString() else ""

            // 배경 초기화
            dayContainer.background = null

            // 우선순위: 선택됨 > 오늘 > 일반
            when {
                day.isSelected -> {
                    dayContainer.setBackgroundResource(R.drawable.bg_calendar_selected)
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    dayText.typeface = Typeface.DEFAULT_BOLD
                }
                day.isToday -> {
                    dayContainer.setBackgroundResource(R.drawable.bg_calendar_today)
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.body_1))
                    dayText.typeface = Typeface.DEFAULT_BOLD
                }
                else -> {
                    dayContainer.background = null
                    dayText.setTextColor(
                        if (day.isCurrentMonth)
                            ContextCompat.getColor(itemView.context, R.color.body_1)
                        else
                            ContextCompat.getColor(itemView.context, R.color.caption_1)
                    )
                    dayText.typeface = Typeface.DEFAULT
                }
            }

            // 이벤트 인디케이터 표시
            updateEventIndicators(day)

            // 클릭 리스너 (현재 월의 날짜만 클릭 가능)
            itemView.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClick(day)
                }
            }
        }

        /**
         * 이벤트 인디케이터 업데이트
         * 최적화: 뷰 재사용 및 불필요한 레이아웃 계산 최소화
         */
        private fun updateEventIndicators(day: CalendarDay) {
            // 현재 월이 아니거나 이벤트가 없으면 숨김
            if (!day.isCurrentMonth || day.events.isEmpty()) {
                eventIndicatorLayout.visibility = View.GONE
                eventCountText.visibility = View.GONE
                return
            }

            val eventCount = day.events.size

            if (eventCount <= 3) {
                // 최대 3개까지 점으로 표시
                eventIndicatorLayout.visibility = View.VISIBLE
                eventCountText.visibility = View.GONE

                // 기존 뷰 재사용
                updateDotViews(eventCount)
            } else {
                // 3개 초과 시 (+개수) 텍스트로 표시
                eventIndicatorLayout.visibility = View.GONE
                eventCountText.visibility = View.VISIBLE
                eventCountText.text = "+${eventCount}"
            }
        }

        /**
         * 점 뷰 업데이트 (뷰 재사용)
         */
        private fun updateDotViews(count: Int) {
            val currentChildCount = eventIndicatorLayout.childCount

            // 필요한 만큼의 점만 표시
            for (i in 0 until count) {
                if (i < currentChildCount) {
                    // 기존 뷰 재사용
                    eventIndicatorLayout.getChildAt(i).visibility = View.VISIBLE
                } else {
                    // 새로운 뷰 추가
                    val dot = createDotView()
                    eventIndicatorLayout.addView(dot)
                }
            }

            // 남은 뷰는 숨김 (제거하지 않고 재사용)
            for (i in count until currentChildCount) {
                eventIndicatorLayout.getChildAt(i).visibility = View.GONE
            }
        }

        /**
         * 점 뷰 생성
         */
        private fun createDotView(): View {
            return View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(5), dpToPx(5)
                ).apply {
                    marginEnd = dpToPx(2)
                }
                setBackgroundResource(R.drawable.bg_event_dot)
            }
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * itemView.context.resources.displayMetrics.density).toInt()
        }
    }

    /**
     * AsyncListDiffer를 위한 DiffUtil.ItemCallback
     * 백그라운드 스레드에서 자동으로 실행되어 ANR 방지
     */
    private class DiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.day == newItem.day &&
                    oldItem.month == newItem.month &&
                    oldItem.year == newItem.year
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.isSelected == newItem.isSelected &&
                    oldItem.isToday == newItem.isToday &&
                    oldItem.isCurrentMonth == newItem.isCurrentMonth &&
                    oldItem.events.size == newItem.events.size
        }
    }
}