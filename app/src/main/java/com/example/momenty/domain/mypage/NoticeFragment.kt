package com.example.momenty.domain.mypage

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentNoticeBinding
import com.example.momenty.domain.mypage.RVA.NoticeRVA
import com.example.momenty.domain.mypage.data.NoticeData

class NoticeFragment: Fragment() {
    lateinit var binding: FragmentNoticeBinding

    private var noticeDatas = ArrayList<NoticeData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoticeBinding.inflate(inflater, container, false)

        initListener()
        inputDummyData()
        setRVA()

        return binding.root
    }

    private fun initListener() {
        binding.btnNoticeBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun inputDummyData() {
        noticeDatas.apply {
            clear()
            add(
                NoticeData(
                    "이용 약관 변경 안내",
                    "26-02-14",
                    "이용 약관 변경 안내 내용"
                )
            )

            add(
                NoticeData(
                    "시스템 점검 안내",
                    "26-02-15",
                    "시스템 점검 안내 내용"
                )
            )
        }
    }

    private fun setRVA() {
        val RVAdapter = NoticeRVA(noticeDatas)
        binding.rvNotice.adapter = RVAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.addNoticeData(noticeDatas)
        RVAdapter.setMyItemClickListener(object: NoticeRVA.MyItemClickListener{
            override fun onItemClick(position: Int) {
                val data = RVAdapter.getNoticeData(position)

                this@NoticeFragment.findNavController().navigate(
                    NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                        data.title, data.content
                    )
                )
            }
        })
    }
}