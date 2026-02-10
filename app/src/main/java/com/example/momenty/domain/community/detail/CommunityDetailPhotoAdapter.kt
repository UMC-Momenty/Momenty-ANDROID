package com.example.momenty.domain.community.detail

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemCommunityDetailPhotoBinding


class CommunityDetailPhotoAdapter :
    ListAdapter<Int, CommunityDetailPhotoAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCommunityDetailPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(private val binding: ItemCommunityDetailPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(resId: Int) {
            binding.resId = resId
            binding.executePendingBindings()
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(old: Int, new: Int) = old == new
            override fun areContentsTheSame(old: Int, new: Int) = old == new
        }
    }
}


