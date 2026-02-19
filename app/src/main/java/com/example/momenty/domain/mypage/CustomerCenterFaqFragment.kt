package com.example.momenty.domain.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentCustomerCenterFaqBinding
import com.example.momenty.domain.calendar.RetrofitClient
import com.example.momenty.domain.mypage.RVA.CustomerCenterFaqRVA
import com.example.momenty.domain.mypage.data.CustomerCenterFaqData
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class CustomerCenterFaqFragment: Fragment() {
    lateinit var binding: FragmentCustomerCenterFaqBinding

    @Inject
    lateinit var tokenManager: TokenManager

    private val TAG = "CC_FaqFrag"
    private val questDatas = ArrayList<CustomerCenterFaqData>()
    private var bSuccessApi = false

    private var loadFaqDatas = ArrayList<LoadFaqData>()
    private var loadFaqDetailData: LoadFaqDetailData ?= null

    private val myPageViewModel: MyPageViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: MyPageService = MyPageRetrofitClient.myPageService
                val repository = MyPageRepository(
                    service = service,
                    tokenManager = tokenManager)
                return MyPageViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        RetrofitClient.initialize(tokenManager, requireContext())

        // Mock 모드에서 토큰이 없으면 Mock 로그인 정보 설정
        if (!tokenManager.isLoggedIn()) {
            tokenManager.saveMockLoginInfo()
            android.util.Log.d(TAG, "Mock login info saved: userId=${tokenManager.getUserId()}")
        }

        // ✅ ViewModel에 LocalDataManager 설정
        val localDataManager = com.example.momenty.global.security.LocalDataManager(requireContext())
        myPageViewModel.setLocalDataManager(localDataManager)

        binding = FragmentCustomerCenterFaqBinding.inflate(inflater, container, false)


        //observePerformLoadFaq()
        //observePerformLoadFaqDetail()

        //performLoadFaq()

        if (bSuccessApi) {
            inputApiData()
        } else {
            inputDummyData()
        }

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

    private fun inputApiData() {
        for (iter in loadFaqDatas) {
            //performLoadFaqDetail(iter.faqId)
            if (bSuccessApi) {
                questDatas.add(CustomerCenterFaqData(loadFaqDetailData!!.question, loadFaqDetailData!!.answer))
            }
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

    /*
    private fun performLoadFaq() {
        val accessToken = tokenManager.getAccessToken()
        myPageViewModel.loadFaq(accessToken!!)
    }

    private fun performLoadFaqDetail(faqId: Int) {
        val accessToken = tokenManager.getAccessToken()
        //myPageViewModel.loadFaqDetail(accessToken!!, faqId)
    }

    private fun observePerformLoadFaq() {
        myPageViewModel.loadFaqResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "프로필 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                loadFaqDatas = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "프로필 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "프로필 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun observePerformLoadFaqDetail() {
        myPageViewModel.loadFaqDetailResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(requireActivity(), "프로필 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                loadFaqDetailData = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "프로필 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "프로필 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }*/
}