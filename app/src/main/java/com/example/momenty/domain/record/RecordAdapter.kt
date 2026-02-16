package com.example.momenty.domain.record

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemRecordBinding
import com.example.momenty.domain.record.write.RecordItem

class RecordAdapter : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    private val items = mutableListOf<RecordItem>()

    fun submitList(newItems: List<RecordItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class RecordViewHolder(
        private val binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordItem) {
            binding.tvDate.text = item.date
            binding.tvMoodChip.text = item.mood
            binding.tvContent.text = item.content
            binding.tvMoodText.text = item.title
            binding.ivThumb.setImageResource(item.imageRes) // 지금은 placeholder
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
