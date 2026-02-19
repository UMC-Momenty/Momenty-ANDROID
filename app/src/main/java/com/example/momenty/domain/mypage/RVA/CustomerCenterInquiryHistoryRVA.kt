package com.example.momenty.domain.mypage.RVA

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.momenty.databinding.ItemInquiryHistoryBinding
import com.example.momenty.domain.mypage.data.CustomerCenterInquiryHistoryData
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CustomerCenterInquiryHistoryRVA(private val historyList: ArrayList<CustomerCenterInquiryHistoryData>)
    : RecyclerView.Adapter<CustomerCenterInquiryHistoryRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(history: CustomerCenterInquiryHistoryData)
        }

    private lateinit var mItemClickListener: MyItemClickListener

    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding: ItemInquiryHistoryBinding = ItemInquiryHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
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
            binding.tvInquiryHistoryDate.text = calculateDaysAgo(data.date)
            binding.tvInquiryHistoryType.text = data.inquiryType
            binding.tvInquiryHistoryContent.text = data.inquiryContent
            if (data.photo != null) {
                var cnt = 0
                for (iter in data.photo) {
                    when (cnt) {
                        0 -> {
                            Glide.with(binding.root.context)
                                .load(Uri.parse(iter.imageUrl))
                                .circleCrop()
                                .into(binding.ivInquiryHistoryPhoto1)
                            binding.ivInquiryHistoryPhoto1.visibility = View.VISIBLE
                        }
                        1 -> {
                            Glide.with(binding.root.context)
                                .load(Uri.parse(iter.imageUrl))
                                .circleCrop()
                                .into(binding.ivInquiryHistoryPhoto2)
                            binding.ivInquiryHistoryPhoto2.visibility = View.VISIBLE
                        }
                    }
                    cnt++
                }
                if (cnt == 0) {
                    binding.layoutInquiryPhoto.visibility = View.GONE
                }
            }
//            binding.ivInquiryHistoryPhoto1.setImageResource(아이디)
//            binding.ivInquiryHistoryPhoto2.setImageResource(아이디)
            binding.tvInquiryHistoryAnswer.text = data.answer
        }
    }

    private fun calculateDaysAgo(dateString: String): String {
        val inputDate = Instant.parse(dateString)
        val now = Instant.now()

        val duration = Duration.between(inputDate, now)
        val seconds = duration.seconds

        return when {
            seconds < 60 -> "방금 전"
            seconds < 3600 -> "${seconds / 60}분 전"
            seconds < 86400 -> "${seconds / 3600}시간 전"
            else -> "${seconds / 86400}일 전"
        }
        /*
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd")

        return try {
            val targetDate = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()

            val daysDiff = ChronoUnit.DAYS.between(targetDate, today)

            when {
                daysDiff == 0L -> "오늘"
                daysDiff > 0 -> "${daysDiff}일 전"
                else -> "${daysDiff}일 후(날짜 오류)"
            }
        } catch (e: Exception) {
            "잘못된 날짜 형식"
        }*/
    }
}