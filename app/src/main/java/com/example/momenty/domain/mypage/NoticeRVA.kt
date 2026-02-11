package com.example.momenty.domain.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.databinding.ItemNoticeBinding

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeRVA.viewHolder {
        val binding: ItemNoticeBinding = ItemNoticeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoticeRVA.viewHolder, position: Int) {
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
            binding.tvNoticeDate.text = data.date

        }
    }
}