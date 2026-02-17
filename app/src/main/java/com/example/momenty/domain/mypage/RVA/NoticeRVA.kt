package com.example.momenty.domain.mypage.RVA

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemNoticeBinding
import com.example.momenty.domain.mypage.LoadNoticeData
import com.example.momenty.domain.mypage.LoadNoticeDetailData
import com.example.momenty.domain.mypage._NoticeData
import com.example.momenty.domain.mypage._PageInfoData
import com.example.momenty.domain.mypage.data.NoticeData
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NoticeRVA(private val noticeList: ArrayList<_NoticeData>)
    : RecyclerView.Adapter<NoticeRVA.viewHolder>() {

        interface MyItemClickListener {
            fun onItemClick(position: Int)
        }

    private lateinit var mItemClickListener: MyItemClickListener
    private lateinit var myNoticeData: ArrayList<LoadNoticeDetailData>
    fun setMyItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    fun getNoticeData(position: Int): LoadNoticeDetailData {
        return myNoticeData[position]
    }

    fun addNoticeData(datas: ArrayList<LoadNoticeDetailData>) {
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
        fun bind(data: _NoticeData) {
            binding.tvNoticeTitle.text = data.title
            binding.tvNoticeDate.text = calculateDaysAgo(data.createdAt)
        }
    }

    private fun calculateDaysAgo(dateString: String): String {
        val parsedInstant = Instant.parse(dateString)

        val zoneId = ZoneId.systemDefault()
        val pastDate = parsedInstant.atZone(zoneId).toLocalDate()
        val today = LocalDate.now(zoneId)

        val diffInDays = ChronoUnit.DAYS.between(pastDate, today)

        return when {
            diffInDays == 0L -> "오늘"
            diffInDays > 0 -> "${diffInDays}일 전"
            else -> "${diffInDays}일 후(날짜 오류)"
        }
    }

    /*
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
    }*/
}