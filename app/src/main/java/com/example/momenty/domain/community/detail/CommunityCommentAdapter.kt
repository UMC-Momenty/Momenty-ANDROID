package com.example.momenty.domain.community.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import com.example.momenty.databinding.ItemCommunityCommentBinding

class CommunityCommentAdapter(
    private val onClickMuteReply: (CommunityCommentUiModel) -> Unit,
    private val onClickDelete: (CommunityCommentUiModel) -> Unit,
) : ListAdapter<CommunityCommentUiModel, CommunityCommentAdapter.VH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCommunityCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val isLast = position == currentList.lastIndex

        holder.bind(
            item = item,
            isLast = isLast,
            onClickMuteReply = onClickMuteReply,
            onClickDelete = onClickDelete
        )
    }

    inner class VH(
        private val binding: ItemCommunityCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: CommunityCommentUiModel,
            isLast: Boolean,
            onClickMuteReply: (CommunityCommentUiModel) -> Unit,
            onClickDelete: (CommunityCommentUiModel) -> Unit,
        ) {
            binding.item = item

            binding.divider.visibility = if (isLast) View.GONE else View.VISIBLE
            binding.executePendingBindings()


            binding.btnMore.setOnClickListener { anchor ->
                showMorePopup(
                    anchor = anchor,
                    item = item,
                    onClickMuteReply = onClickMuteReply,
                    onClickDelete = onClickDelete
                )
            }
        }

        private fun showMorePopup(
            anchor: View,
            item: CommunityCommentUiModel,
            onClickMuteReply: (CommunityCommentUiModel) -> Unit,
            onClickDelete: (CommunityCommentUiModel) -> Unit,
        ) {
            val ctx = anchor.context

            val popupView = LayoutInflater.from(ctx)
                .inflate(R.layout.popup_comment_more, null, false)

            val popup = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                isOutsideTouchable = true
                elevation = 12f
            }

            val tvMute = popupView.findViewById<TextView>(R.id.tvMute)
            val tvDelete = popupView.findViewById<TextView>(R.id.tvDelete)

            tvMute.setOnClickListener {
                popup.dismiss()
                onClickMuteReply(item)
            }

            tvDelete.setOnClickListener {
                popup.dismiss()
                onClickDelete(item)
            }


            popupView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            val yOff = -(anchor.height + popupView.measuredHeight)

            popup.showAsDropDown(
                anchor,
                -popupView.measuredWidth + anchor.width,
                yOff
            )
        }
    }

    companion object {
        private val diff = object : DiffUtil.ItemCallback<CommunityCommentUiModel>() {
            override fun areItemsTheSame(old: CommunityCommentUiModel, new: CommunityCommentUiModel) =
                old.id == new.id

            override fun areContentsTheSame(old: CommunityCommentUiModel, new: CommunityCommentUiModel) =
                old == new
        }
    }
}
