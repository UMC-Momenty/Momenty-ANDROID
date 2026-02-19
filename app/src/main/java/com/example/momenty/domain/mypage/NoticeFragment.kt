package com.example.momenty.domain.mypage

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentNoticeBinding
import com.example.momenty.domain.calendar.RetrofitClient
import com.example.momenty.domain.mypage.RVA.NoticeRVA
import com.example.momenty.domain.mypage.data.NoticeData
import com.example.momenty.global.security.TokenManager
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.getValue

class NoticeFragment: Fragment() {
    lateinit var binding: FragmentNoticeBinding
    lateinit var tokenManager: TokenManager
    private var bSuccessApi = false

    private val TAG = "NoticeFrag"

    private var loadNoticeByApi: LoadNoticeData<_NoticeDatas, _PageInfoData> ?= null
    private var noticeDetailDataListByApi = ArrayList<LoadNoticeDetailData>()

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


        binding = FragmentNoticeBinding.inflate(inflater, container, false)


        observePerformLoadNotice()
        observePerformLoadNoticeDetail()

        initListener()
        performLoadNotice()

        setRVA()

        return binding.root
    }

    private fun initListener() {
        binding.btnNoticeBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadNoticeDetail(dataList: LoadNoticeData<_NoticeDatas, _PageInfoData>?) {
        noticeDetailDataListByApi.clear()
        if (dataList != null) {
            for (iter in dataList.notices) {
                performLoadNoticeDetail(iter.noticeId)
            }
        } else {
            inputDummyData()
        }
        binding.rvNotice.adapter?.notifyDataSetChanged()
    }

    private fun inputDummyData() {
        noticeDetailDataListByApi.apply {
            add(
                LoadNoticeDetailData(
                0, "이용 약관 변경 안내",
                "이용 약관 변경 안내 내용", "2026-02-16T13:09:49.627Z")
            )

            add(
                LoadNoticeDetailData(
                    0, "시스템 점검 안내",
                    "시스템 점검 안내 내용", "2026-02-16T13:09:49.627Z")
            )
        }
    }

    private fun setRVA() {
        val RVAdapter = NoticeRVA(noticeDetailDataListByApi)
        binding.rvNotice.adapter = RVAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.setMyItemClickListener(object: NoticeRVA.MyItemClickListener{
            override fun onItemClick(position: Int) {
                val data = noticeDetailDataListByApi[position]

                this@NoticeFragment.findNavController().navigate(
                    NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                        title = data.title,
                        content = data.content
                    )
                )
            }
        })
    }


    private fun performLoadNotice() {
        myPageViewModel.loadNotice()
    }

    private fun observePerformLoadNotice() {
        myPageViewModel.loadNoticeResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "공지 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                bSuccessApi = true

                loadNoticeByApi = data
                loadNoticeDetail(loadNoticeByApi)

                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "공지 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "공지 로드 실패: $message")

                //inputDummyData()
                bSuccessApi = false
            }
        }
    }

    private fun performLoadNoticeDetail(noticeId: Long) {
        myPageViewModel.loadNoticeDetail(noticeId)
    }

    private fun observePerformLoadNoticeDetail() {
        myPageViewModel.loadNoticeDetailResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "공지 세부 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")

                noticeDetailDataListByApi.add(data)

                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "공지 세부 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "공지 세부 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }
}