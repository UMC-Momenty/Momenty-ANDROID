package com.example.momenty.domain.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCustomerCenterInquiryHistoryBinding
import com.example.momenty.domain.calendar.RetrofitClient
import com.example.momenty.domain.mypage.RVA.CustomerCenterInquiryHistoryRVA
import com.example.momenty.domain.mypage.data.CustomerCenterInquiryHistoryData
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class CustomerCenterInquiryHistoryFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterInquiryHistoryBinding

    @Inject
    lateinit var tokenManager: TokenManager
    private val TAG = "InqHisFrag"
    private var bSuccessApi = false

    private var historyDatas = ArrayList<CustomerCenterInquiryHistoryData>()
    private var loadInquiryDataByApi: LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo> ?= null
    private var loadInquiryDataInquiriesByApi: LoadInquiryDataInquiries ?= null
    private var loadInquiryDetailDataByApi: LoadInquiryDetailData<LoadInquiryDetailDataImages> ?= null

    private val myPageViewModel: MyPageViewModel by activityViewModels {
        val repo = MyPageRepository(service = MyPageRetrofitClient.myPageService)
        MyPageViewModelFactory(repo)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MyPageRetrofitClient.initialize(tokenManager, requireContext())

        // Mock 모드에서 토큰이 없으면 Mock 로그인 정보 설정
        if (!tokenManager.isLoggedIn()) {
            tokenManager.saveMockLoginInfo()
        }

        // ✅ ViewModel에 LocalDataManager 설정
        val localDataManager = com.example.momenty.global.security.LocalDataManager(requireContext())
        myPageViewModel.setLocalDataManager(localDataManager)

        binding = FragmentCustomerCenterInquiryHistoryBinding.inflate(inflater, container, false)

        observePerformLoadInquiry()
        observePerformLoadInquiryDetail()

        setRVA()
        performLoadInquiry()
        /*
        if (bSuccessApi) {
            getInquiryDetailData()
        } else {
            inputDummyData()
        }*/

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

        binding.rvInquiryHistory.adapter?.notifyDataSetChanged()
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
        if (bSuccessApi) {
            historyDatas.apply {
                clear()
                Log.e(TAG, "getInquiryDetailData 진입")
                for (iter in loadInquiryDataByApi!!.inquiries){
                    loadInquiryDataInquiriesByApi = iter
                    Log.e(TAG, "for문 진입")
                    performLoadInquiryDetail(iter.inquiryId)
                }
            }

        }

    }

    private fun setInquiryDetailData(inquiries: LoadInquiryDataInquiries?) {
        if (bSuccessApi && loadInquiryDetailDataByApi != null && inquiries != null) {
            Log.e(TAG, "세부 문의내역 데이터 작성")
            historyDatas.add(CustomerCenterInquiryHistoryData(
                content2Title(loadInquiryDetailDataByApi!!.content),
                inquiries.createdAt,
                inquiries.type,
                loadInquiryDetailDataByApi!!.content,
                loadInquiryDetailDataByApi!!.images,
                ""
            ))
            binding.rvInquiryHistory.adapter?.notifyDataSetChanged()
        }
    }


    private fun performLoadInquiry() {
        myPageViewModel.loadInquiry()
    }

    private fun observePerformLoadInquiry() {
        myPageViewModel.loadInquiryResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(requireContext(), "문의내역 로드 성공!", Toast.LENGTH_SHORT).show()
                loadInquiryDataByApi = data
                Log.d(TAG, "문의내역 로드 성공: $data")
                getInquiryDetailData()
                checkHistoryEmpty()

                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireContext(), "문의내역 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "문의내역 로드 실패: $message")
                checkHistoryEmpty()

                bSuccessApi = false
            }
        }
    }

    private fun performLoadInquiryDetail(inquiryId: Long) {
        myPageViewModel.loadInquiryDetail(inquiryId)
    }


    private fun observePerformLoadInquiryDetail() {
        myPageViewModel.loadInquiryDetailResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(requireContext(), "세부 문의내역 로드 성공!", Toast.LENGTH_SHORT).show()

                loadInquiryDetailDataByApi = data
                setInquiryDetailData(loadInquiryDataInquiriesByApi)
                checkHistoryEmpty()

                Log.d(TAG, "세부 문의내역 로드 성공: $data")
                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"

                Toast.makeText(requireContext(), "세부 문의내역 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "문의내역 로드 실패: $message")
                checkHistoryEmpty()
                inputDummyData()

                bSuccessApi = false
            }
        }
    }

    private fun content2Title(input: String): String {
        return if (input.length >= 7) {
            input.take(7) + "..."
        } else {
            input
        }
    }

    private fun checkHistoryEmpty() {
        if (!historyDatas.isEmpty()) {
            binding.layoutInquiryHistoryNoData.visibility = View.VISIBLE
            binding.rvInquiryHistory.visibility = View.GONE
        } else {
            binding.layoutInquiryHistoryNoData.visibility = View.GONE
            binding.rvInquiryHistory.visibility = View.VISIBLE
        }
    }
}