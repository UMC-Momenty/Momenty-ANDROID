package com.example.momenty.domain.community.write


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemCommunityWritePhotoBinding


class WritePhotoAdapter(
    private val onAddClick: () -> Unit,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<WritePhotoAdapter.VH>() {

    private val MAX_COUNT = 10
    private var photos: List<Uri> = emptyList()

    fun submitList(list: List<Uri>) {
        photos = list.take(MAX_COUNT)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (photos.size < MAX_COUNT) {
            photos.size + 1
        } else {
            photos.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCommunityWritePhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }

    inner class VH(
        private val binding: ItemCommunityWritePhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {

            if (position == photos.size && photos.size < MAX_COUNT) {
                binding.ivThumb.setImageResource(R.drawable.ic_community_plus)
                binding.btnRemove.visibility = View.GONE
                binding.root.setOnClickListener { onAddClick() }
                return
            }

            val uri = photos[position]

            Glide.with(binding.ivThumb)
                .load(uri)
                .centerCrop()
                .into(binding.ivThumb)

            binding.btnRemove.visibility = View.VISIBLE
            binding.btnRemove.setOnClickListener {
                onRemoveClick(uri)
            }

            binding.root.setOnClickListener(null)
        }
    }
}
