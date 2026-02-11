package com.example.momenty.domain.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemInquiryHistoryBinding

class CustomerCenterInquiryHistoryRVA(private val historyList: ArrayList<CustomerCenterInquiryHistoryData>)
    : RecyclerView.Adapter<CustomerCenterInquiryHistoryRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(history: CustomerCenterInquiryHistoryData)
        }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerCenterInquiryHistoryRVA.viewHolder {
        val binding: ItemInquiryHistoryBinding = ItemInquiryHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerCenterInquiryHistoryRVA.viewHolder, position: Int) {
        holder.bind(historyList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(historyList[position])
        }

        holder.binding.ivInquiryHistoryArrowDown.setOnClickListener {
            holder.binding.layoutInquiryHistoryDetail.visibility = View.VISIBLE
            holder.binding.ivInquiryHistoryArrowUp.visibility = View.VISIBLE
            holder.binding.ivInquiryHistoryArrowDown.visibility = View.GONE
        }
        holder.binding.ivInquiryHistoryArrowUp.setOnClickListener {
            holder.binding.layoutInquiryHistoryDetail.visibility = View.GONE
            holder.binding.ivInquiryHistoryArrowUp.visibility = View.GONE
            holder.binding.ivInquiryHistoryArrowDown.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    inner class viewHolder(val binding: ItemInquiryHistoryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CustomerCenterInquiryHistoryData) {
            binding.tvInquiryHistoryTitle.text = data.title
            binding.tvInquiryHistoryDate.text = data.date
            binding.tvInquiryHistoryType.text = data.inquiryType
            binding.tvInquiryHistoryContent.text = data.inquiryContent
//            binding.ivInquiryHistoryPhoto1.setImageResource(아이디)
//            binding.ivInquiryHistoryPhoto2.setImageResource(아이디)
            binding.tvInquiryHistoryAnswer.text = data.answer
        }
    }
}