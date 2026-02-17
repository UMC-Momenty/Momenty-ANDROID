package com.example.momenty.domain.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCustomerCenterInquiryHistoryBinding
import com.example.momenty.domain.home.KhgApiClient
import com.example.momenty.domain.mypage.RVA.CustomerCenterInquiryHistoryRVA
import com.example.momenty.domain.mypage.data.CustomerCenterInquiryHistoryData
import com.example.momenty.global.security.TokenManager
import kotlin.getValue

class CustomerCenterInquiryHistoryFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterInquiryHistoryBinding
    lateinit var tokenManager: TokenManager
    private val TAG = "InqHisFrag"
    private var bSuccessApi = false

    private var historyDatas = ArrayList<CustomerCenterInquiryHistoryData>()
    private var loadInquiryData: LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo> ?= null
    private var loadInquiryDetailData: LoadInquiryDetailData<LoadInquiryDetailDataImages> ?= null

    private val myPageViewModel: MyPageViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: MyPageService = KhgApiClient.myPageService
                val repository = MyPageRepository(service)
                return MyPageViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerCenterInquiryHistoryBinding.inflate(inflater, container, false)
        tokenManager = TokenManager(requireContext())

        observePerformLoadInquiry()
        observePerformLoadInquiryDetail()

        performLoadInquiry()
        if (bSuccessApi) {
            getInquiryDetailData()
        } else {
            inputDummyData()
        }
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
                    //null,
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
                    //null,
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

    private fun getInquiryDetailData() {
        historyDatas.apply {
            clear()



            for (iter in loadInquiryData!!.inquiries){
                performLoadInquiryDetail(iter.inquiryId)
                if (loadInquiryDetailData != null) {
                    add(CustomerCenterInquiryHistoryData(
                        "서비스 이용 안내", // TODO: 더미데이터. API에 제목?? 없다???
                        iter.createdAt,
                        iter.type,
                        loadInquiryDetailData!!.content,
                        loadInquiryDetailData!!.images,
                        ""
                    ))
                }
            }
        }
    }

    private fun performLoadInquiry() {
        val accessToken = tokenManager.getAccessToken()
        val userId = tokenManager.getUserId()
        myPageViewModel.loadInquiry(accessToken!!, userId)
    }

    private fun performLoadInquiryDetail(inquiryId: Int) {
        val accessToken = tokenManager.getAccessToken()
        myPageViewModel.loadInquiryDetail(accessToken!!, inquiryId)
    }

    private fun observePerformLoadInquiry() {
        myPageViewModel.loadInquiryResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "문의내역 로드 성공!", Toast.LENGTH_SHORT).show()
                loadInquiryData = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireContext(), "문의내역 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "문의내역 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun observePerformLoadInquiryDetail() {
        myPageViewModel.loadInquiryDetailResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireContext(), "문의내역 로드 성공!", Toast.LENGTH_SHORT).show()
                loadInquiryDetailData = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireContext(), "문의내역 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "문의내역 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }
}