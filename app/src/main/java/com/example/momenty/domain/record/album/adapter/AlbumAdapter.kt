package com.example.momenty.domain.record.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemAlbumBinding
import com.example.momenty.domain.record.album.model.AlbumUiModel

class AlbumAdapter(
    private val onClick: (AlbumUiModel) -> Unit
) : RecyclerView.Adapter<AlbumAdapter.VH>() {

    private val items = mutableListOf<AlbumUiModel>()

    fun submitList(list: List<AlbumUiModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlbumUiModel) {
            binding.ivThumb.setImageResource(item.thumbRes)
            binding.tvTitle.text = item.title
            binding.tvCount.text = item.count.toString()
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
