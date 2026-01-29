package com.example.momenty.domain.community


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.databinding.ItemWritePhotoBinding


class WritePhotoAdapter(
    private val onAddClick: () -> Unit,
    private val onRemoveClick: (Uri) -> Unit
) : RecyclerView.Adapter<WritePhotoAdapter.VH>() {

    private var items: List<Uri> = emptyList()

    fun submitList(list: List<Uri>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = 1 + items.size   // + 버튼 포함

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemWritePhotoBinding.inflate(
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
        private val binding: ItemWritePhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            if (position == 0) {

                binding.ivThumb.setImageResource(R.drawable.ic_community_plus)
                binding.btnRemove.visibility = View.GONE
                binding.root.setOnClickListener { onAddClick() }
            } else {
                val uri = items[position - 1]
                Glide.with(binding.ivThumb)
                    .load(uri)
                    .centerCrop()
                    .into(binding.ivThumb)

                binding.btnRemove.visibility = View.VISIBLE
                binding.btnRemove.setOnClickListener {
                    onRemoveClick(uri)
                }
            }
        }
    }
}
