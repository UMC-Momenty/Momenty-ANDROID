package com.example.momenty.domain.record

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.databinding.ItemRecordWritePhotoPagerBinding

class PhotoPagerAdapter(
    private val items: MutableList<Uri> = mutableListOf()
) : RecyclerView.Adapter<PhotoPagerAdapter.PhotoVH>() {

    inner class PhotoVH(val binding: ItemRecordWritePhotoPagerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoVH {
        val binding = ItemRecordWritePhotoPagerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoVH(binding)
    }

    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
        val uri = items[position]
        Glide.with(holder.binding.ivPhoto)
            .load(uri)
            .into(holder.binding.ivPhoto)
    }

    override fun getItemCount(): Int = items.size



    fun submitList(newItems: List<Uri>, onComplete: (() -> Unit)? = null) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
        onComplete?.invoke()
    }

}
