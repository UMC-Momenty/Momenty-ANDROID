package com.example.momenty.domain.mypage.RVA

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemNoticeBinding
import com.example.momenty.domain.mypage.data.NoticeData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NoticeRVA(private val noticeList: ArrayList<NoticeData>)
    : RecyclerView.Adapter<NoticeRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(position: Int)
        }

    private lateinit var mItemClickListener: MyItemClickListener
    private lateinit var myNoticeData: ArrayList<NoticeData>
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    fun getNoticeData(position: Int): NoticeData {
        return myNoticeData[position]
    }

    fun addNoticeData(datas: ArrayList<NoticeData>) {
        myNoticeData = datas
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding: ItemNoticeBinding = ItemNoticeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(noticeList[position])
        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return noticeList.size
    }

    inner class viewHolder(val binding: ItemNoticeBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NoticeData) {
            binding.tvNoticeTitle.text = data.title
            binding.tvNoticeDate.text = calculateDaysAgo(data.date)

        }
    }

    private fun calculateDaysAgo(dateString: String): String {
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
        }
    }
}