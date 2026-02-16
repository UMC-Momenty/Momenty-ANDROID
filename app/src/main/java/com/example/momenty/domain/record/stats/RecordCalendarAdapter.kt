package com.example.momenty.domain.record.stats

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R

class RecordCalendarAdapter : RecyclerView.Adapter<RecordCalendarAdapter.VH>() {

    private val items = mutableListOf<RecordCalendarDay>()

    fun submit(newItems: List<RecordCalendarDay>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record_calendar_day, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)

        fun bind(item: RecordCalendarDay) {
            // 빈칸(앞/뒤 padding)
            if (!item.isCurrentMonth || item.day == 0) {
                tvDay.text = ""
                tvDay.isClickable = false
                return
            }

            tvDay.text = item.day.toString()
            tvDay.typeface = Typeface.DEFAULT

            val defaultColor = ContextCompat.getColor(itemView.context, R.color.body_1)
            val recordColor = ContextCompat.getColor(itemView.context, R.color.primary)

            tvDay.setTextColor(if (item.hasRecord) recordColor else defaultColor)
        }
    }
}
