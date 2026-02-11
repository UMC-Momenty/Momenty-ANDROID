package com.example.momenty.domain.community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemCommunityPostBinding

class CommunityAdapter(
    private val onClick: (CommunityPostUiModel) -> Unit
) : ListAdapter<CommunityPostUiModel, CommunityAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCommunityPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemCommunityPostBinding,
        private val onClick: (CommunityPostUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommunityPostUiModel) = with(binding) {
            tvBadge.text = item.category.label
            tvAuthor.text = item.author
            tvDate.text = item.dateText
            tvTitle.text = item.title
            tvBody.text = item.content

            tvBody.post {
                val over = tvBody.lineCount > 5
                tvMore.visibility = if (over) View.VISIBLE else View.GONE
            }

            tvLikeCount.text = item.likeCount.toString()
            tvCommentCount.text = item.commentCount.toString()

            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<CommunityPostUiModel>() {
            override fun areItemsTheSame(old: CommunityPostUiModel, new: CommunityPostUiModel) = old.id == new.id
            override fun areContentsTheSame(old: CommunityPostUiModel, new: CommunityPostUiModel) = old == new
        }
    }
}
