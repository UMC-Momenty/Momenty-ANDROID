package com.example.momenty.domain.record

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.databinding.ItemRecordBinding

class RecordAdapter(
    private val items: List<RecordItem>
) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    inner class RecordViewHolder(
        private val binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecordItem) {
            binding.tvDate.text = item.date
            binding.tvMoodChip.text = item.mood
            binding.tvContent.text = item.content
            binding.tvMoodText.text = item.title
            binding.ivThumb.setImageResource(item.imageRes)
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
