package com.example.momenty.domain.record.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemAlbumPhotoBinding

class AlbumDetailPhotoAdapter : ListAdapter<Int, AlbumDetailPhotoAdapter.VH>(diff) {

    inner class VH(val binding: ItemAlbumPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlbumPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.ivPhoto.setImageResource(getItem(position))
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
            override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        }
    }
}
