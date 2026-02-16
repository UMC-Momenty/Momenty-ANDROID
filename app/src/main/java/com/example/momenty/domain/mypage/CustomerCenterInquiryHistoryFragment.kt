package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCustomerCenterInquiryHistoryBinding
import com.example.momenty.domain.mypage.RVA.CustomerCenterInquiryHistoryRVA
import com.example.momenty.domain.mypage.data.CustomerCenterInquiryHistoryData

class CustomerCenterInquiryHistoryFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterInquiryHistoryBinding

    private var historyDatas = ArrayList<CustomerCenterInquiryHistoryData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterInquiryHistoryBinding.inflate(inflater, container, false)

        inputDummyData()
        setRVA()

        return binding.root
    }

    private fun inputDummyData() {
        historyDatas.apply {
            clear()
            add(
                CustomerCenterInquiryHistoryData(
                    "서비스 이용 안내",
                    "26-02-10",
                    "계정",
                    "로그아웃 시 내용이 삭제되나요?",
                    null,
                    null,
                    "삭제되지 않습니다."
                )
            )

            add(
                CustomerCenterInquiryHistoryData(
                    "서비스 이용 안내2",
                    "26-02-11",
                    "계정2",
                    "로그아웃 시 내용이 삭제되나요?2",
                    null,
                    null,
                    "삭제되지 않습니다.2"
                )
            )
        }
    }

    private fun setRVA() {
        val RVAdapter = CustomerCenterInquiryHistoryRVA(historyDatas)
        binding.rvInquiryHistory.adapter = RVAdapter
        binding.rvInquiryHistory.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.setMyItemClickListener(object: CustomerCenterInquiryHistoryRVA.MyItemClickListener{
            override fun onItemClick(history: CustomerCenterInquiryHistoryData) {
                // TODO("Not yet implemented")
            }
        })
    }
}