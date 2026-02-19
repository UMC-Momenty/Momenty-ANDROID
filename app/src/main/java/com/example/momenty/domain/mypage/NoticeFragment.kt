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

    //private var noticeDatas = ArrayList<NoticeData>()
    private var noticeDatas = ArrayList<_NoticeData>()
    private var test_noticeDatas = ArrayList<_NoticeData>()
    private var loadNoticeByApi = ArrayList<LoadNoticeData<_NoticeData, _PageInfoData>>()
    private var noticeDetailData: LoadNoticeDetailData ?= null
    private var test_noticeDetailList = ArrayList<LoadNoticeDetailData>()

    private val myPageViewModel: MyPageViewModel by activityViewModels {
        val repo = MyPageRepository(
            service = MyPageRetrofitClient.myPageService,
            tokenManager = tokenManager
        )
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
            android.util.Log.d(TAG, "Mock login info saved: userId=${tokenManager.getUserId()}")
        }

        // ✅ ViewModel에 LocalDataManager 설정
        val localDataManager = com.example.momenty.global.security.LocalDataManager(requireContext())
        myPageViewModel.setLocalDataManager(localDataManager)


        binding = FragmentNoticeBinding.inflate(inflater, container, false)


        //observePerformLoadNotice()
        //observePerformLoadNoticeDetail()

        initListener()
        inputDummyData()
        //performLoadNotice()
        loadData()

        setRVA()

        return binding.root
    }

    private fun initListener() {
        binding.btnNoticeBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadData() {
        if (!loadNoticeByApi.isNullOrEmpty()) {
            Log.e(TAG, "api 요청 성공")
            noticeDatas.clear()
            for (data in loadNoticeByApi[0].notices) {
                noticeDatas.add(data)
            }
        } else {
            Log.e(TAG, "api 요청 실패")
            noticeDatas.clear()
            for (data in test_noticeDatas) {
                noticeDatas.add(data)
            }
        }
    }


    private fun inputDummyData() {
        test_noticeDatas.apply {
            clear()
            add(_NoticeData(0, "이용 약관 변경 안내", "2026-02-16T13:09:49.627Z"))
            add(_NoticeData(1, "시스템 점검 안내", "2026-02-16T13:09:49.627Z"))
        }
        test_noticeDetailList.apply {
            clear()
            add(LoadNoticeDetailData(0, "이용 약관 변경 안내",
                "이용 약관 변경 안내 내용", "2026-02-16T13:09:49.627Z"))
            add(LoadNoticeDetailData(1, "시스템 점검 안내",
                "시스템 점검 안내 내용", "2026-02-16T13:09:49.627Z"))
        }
        /*
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
        }*/
    }

    private fun setRVA() {
        val RVAdapter = NoticeRVA(noticeDatas)
        binding.rvNotice.adapter = RVAdapter
        binding.rvNotice.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )

        RVAdapter.setMyItemClickListener(object: NoticeRVA.MyItemClickListener{
            override fun onItemClick(position: Int) {
                //performLoadNoticeDetail(noticeDatas[position].noticeId)

                if (bSuccessApi) {
                    Log.e(TAG, "api 요청 성공")
                    val title = noticeDetailData?.title
                    val content = noticeDetailData?.content
                    this@NoticeFragment.findNavController().navigate(
                        NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                            title!!, content!!
                        )
                    )
                } else {
                    Log.e(TAG, "api 요청 실패 ${noticeDatas[position].noticeId}")
                    //noticeDetailData = test_noticeDetailList[noticeDatas[position].noticeId]
                    val title = noticeDetailData?.title
                    val content = noticeDetailData?.content
                    this@NoticeFragment.findNavController().navigate(
                        NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                            title!!, content!!
                        )
                    )
                }

                /*
                val data = RVAdapter.getNoticeData(position)

                this@NoticeFragment.findNavController().navigate(
                    NoticeFragmentDirections.actionNoticeFragmentToNoticeDetailFragment(
                        data.title, data.content
                    )
                )*/
            }
        })
    }

    /*
    private fun performLoadNotice() {
        val accessToken = tokenManager.getAccessToken()
        myPageViewModel.loadNotice(accessToken!!)
    }

    private fun observePerformLoadNotice() {
        myPageViewModel.loadNoticeResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "공지 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                loadNoticeByApi.apply {
                    clear()
                    add(data)
                }
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "공지 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "공지 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun performLoadNoticeDetail(noticeId: Int=1) {
        val accessToken = tokenManager.getAccessToken()
        myPageViewModel.loadNoticeDetail(accessToken!!, noticeId)
    }

    private fun observePerformLoadNoticeDetail() {
        myPageViewModel.loadNoticeDetailResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "공지 세부 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                noticeDetailData = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "공지 세부 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "공지 세부 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }*/

    /*
    private fun getFormattedDate(date: String): String {
        val parsedDate = ZonedDateTime.parse(date)
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd")
        val result = parsedDate.format(formatter)

        return result
    }*/
}