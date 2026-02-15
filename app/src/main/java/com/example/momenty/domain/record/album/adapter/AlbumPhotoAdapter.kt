package com.example.momenty.domain.record.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemAlbumPhotoBinding
import com.example.momenty.domain.record.album.model.AlbumPhotoUiModel

class AlbumPhotoAdapter : RecyclerView.Adapter<AlbumPhotoAdapter.VH>() {

    private val items = mutableListOf<AlbumPhotoUiModel>()

    fun submitList(list: List<AlbumPhotoUiModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(private val binding: ItemAlbumPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlbumPhotoUiModel) {
            binding.ivPhoto.setImageResource(item.photoRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlbumPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}
