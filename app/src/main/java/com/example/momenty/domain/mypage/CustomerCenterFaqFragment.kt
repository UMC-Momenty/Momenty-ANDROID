package com.example.momenty.domain.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCustomerCenterFaqBinding
import com.example.momenty.domain.mypage.RVA.CustomerCenterFaqRVA
import com.example.momenty.domain.mypage.data.CustomerCenterFaqData

class CustomerCenterFaqFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterFaqBinding
    private val questDatas = ArrayList<CustomerCenterFaqData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterFaqBinding.inflate(inflater, container, false)

        inputDummyData()
        setRVA()
        checkQuestEmpty()

        return binding.root
    }

    private fun inputDummyData() {
        questDatas.apply {
            add(CustomerCenterFaqData("Q. 이 서비스는 어떤 앱인가요?", "반려동물 기록 앱"))
            add(CustomerCenterFaqData("Q. 어떤 내용을 기록할 수 있나요?", "감정, 질문, 사진"))
        }
    }

    private fun setRVA() {
        val RVAdapter = CustomerCenterFaqRVA(questDatas)
        binding.rvFaq.adapter = RVAdapter
        binding.rvFaq.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.setMyItemClickListener(object: CustomerCenterFaqRVA.MyItemClickListener{
            override fun onItemClick(data: CustomerCenterFaqData) {
                // TODO("Not yet implemented")
            }
        })
    }

    private fun checkQuestEmpty() {
        if (!questDatas.isEmpty()) {
            binding.layoutFaqEmpty.visibility = View.GONE
            binding.rvFaq.visibility = View.VISIBLE
        } else {
            binding.layoutFaqEmpty.visibility = View.VISIBLE
            binding.rvFaq.visibility = View.GONE
        }
    }
}