package com.example.momenty.domain.mypage.RVA

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemFaqBinding
import com.example.momenty.databinding.ItemInquiryHistoryBinding
import com.example.momenty.domain.mypage.data.CustomerCenterFaqData
import com.example.momenty.domain.mypage.data.CustomerCenterInquiryHistoryData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CustomerCenterFaqRVA(private val questList: ArrayList<CustomerCenterFaqData>)
    : RecyclerView.Adapter<CustomerCenterFaqRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(data: CustomerCenterFaqData)
        }

    private lateinit var mItemClickListener: MyItemClickListener
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding: ItemFaqBinding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(questList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(questList[position])
        }

        holder.binding.ivFaqArrowDown.setOnClickListener {
            holder.binding.layoutInquiryHistoryDetail.visibility = View.VISIBLE
            holder.binding.ivFaqArrowUp.visibility = View.VISIBLE
            holder.binding.ivFaqArrowDown.visibility = View.GONE
        }
        holder.binding.ivFaqArrowUp.setOnClickListener {
            holder.binding.layoutInquiryHistoryDetail.visibility = View.GONE
            holder.binding.ivFaqArrowUp.visibility = View.GONE
            holder.binding.ivFaqArrowDown.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return questList.size
    }

    inner class viewHolder(val binding: ItemFaqBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CustomerCenterFaqData) {
            binding.tvFaqQuest.text = "Q " + data.quest
            binding.tvInquiryHistoryAnswer.text = data.answer
        }
    }
}