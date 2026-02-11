package com.example.momenty.domain.calendar

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimePickerAdapter(
    private val items: List<Int>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<TimePickerAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = TextView(parent.context).apply {
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setPadding(16, 24, 16, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount() = items.size

    fun setSelected(value: Int) {
        val newPosition = items.indexOf(value)
        if (newPosition != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(newPosition)
        }
    }

    inner class ViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(value: Int, isSelected: Boolean) {
            textView.text = String.format("%02d", value)
            textView.isSelected = isSelected
            textView.setOnClickListener {
                onItemClick(value)
                setSelected(value)
            }
        }
    }
}