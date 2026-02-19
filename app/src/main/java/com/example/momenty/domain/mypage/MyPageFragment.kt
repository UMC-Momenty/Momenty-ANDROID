package com.example.momenty.domain.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.momenty.BuildConfig
import com.example.momenty.R
import com.example.momenty.databinding.FragmentMyPageBinding
import com.example.momenty.domain.calendar.RetrofitClient
import com.example.momenty.domain.home.LogoutDialog
import com.example.momenty.domain.home.MyLogoutInterface
import com.example.momenty.domain.home.MyUnsubscribeInterface
import com.example.momenty.domain.home.UnsubscribeDialog
import com.example.momenty.domain.main.presentation.MainActivity
import com.example.momenty.domain.mypage.RVA.MyPageRVA
import com.example.momenty.domain.mypage.RVA.NoticeRVA
import com.example.momenty.domain.mypage.data.MyPagePetProfileData
import com.example.momenty.domain.mypage.data.NoticeData
import com.example.momenty.global.mock.MockApiInterceptor
import com.example.momenty.global.security.TokenManager
import com.google.gson.Gson
import com.kakao.sdk.user.model.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class MyPageFragment : Fragment(), MyLogoutInterface, MyUnsubscribeInterface {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val TAG = "MyPageFrag"

    lateinit var binding: FragmentMyPageBinding

    @Inject
    lateinit var tokenManager: TokenManager
    private var bSuccessApi = false
    lateinit var rvAdapter: MyPageRVA

    private var loadProfileByApi: LoadProfileData ?= null

    private var petProfileDatas = ArrayList<MyPagePetProfileData>()

    /*
    private val myPageViewModel: MyPageViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: MyPageService = KhgApiClient.myPageService
                val repository = MyPageRepository(service)
                return MyPageViewModel(repository) as T
            }
        }
    }*/

    private val myPageViewModel: MyPageViewModel by activityViewModels {
        val repo = MyPageRepository(service = MyPageRetrofitClient.myPageService)
        MyPageViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        performLoadProfile()
        setName()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MyPageRetrofitClient.initialize(tokenManager, requireContext())

        // Mock 모드에서 토큰이 없으면 Mock 로그인 정보 설정
        if (!tokenManager.isLoggedIn()) {
            tokenManager.saveMockLoginInfo()
        }

        // ✅ ViewModel에 LocalDataManager 설정
        val localDataManager = com.example.momenty.global.security.LocalDataManager(requireContext())
        myPageViewModel.setLocalDataManager(localDataManager)

        observePerformLoadProfile()
        observePerformLogout()

        performLoadProfile()

        initListener()
        setRVA()
        setName()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_my_page, container, false)

        binding = FragmentMyPageBinding.inflate(layoutInflater)

        Log.e(TAG, "Mock enabled?: ${MockApiInterceptor.isMockEnabled}")



        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyPageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun initListener() {
        binding.btnMyPageManageProfile.setOnClickListener {
            activityTransition(UserProfileActivity())
        }
        binding.layoutMyPageAddPet.setOnClickListener {
            activityTransition(PetFormAddActivity())
        }
        binding.tvMyPageNotification.setOnClickListener {
            findNavController().navigate(R.id.notificationFragment)
        }
        binding.tvMyPageNotice.setOnClickListener {
            findNavController().navigate(R.id.noticeFragment)
        }
        binding.tvMyPageCustomerCenter.setOnClickListener {
            findNavController().navigate(R.id.customerCenterFragment)
        }
        binding.tvMyPageAppInfo.setOnClickListener {
            findNavController().navigate(R.id.appInfoFragment)
        }
        binding.tvMyPageLogout.setOnClickListener {
            val logoutDialog = LogoutDialog(this)
            logoutDialog.show(childFragmentManager, "LogoutDialog")
        }
        binding.tvMyPageUnsubscribe.setOnClickListener {
            val unsubscribeDialog = UnsubscribeDialog(this)
            unsubscribeDialog.show(childFragmentManager, "UnsubscribeDialog")
        }

    }

    private fun activityTransition(act: AppCompatActivity){
        val intent = Intent(requireActivity(), act::class.java)
        startActivity(intent)
    }

    private fun setName() {
        if (bSuccessApi && loadProfileByApi != null) {
            Log.e(TAG, "api 로드 성공")

            setUserProfile(loadProfileByApi!!.username, loadProfileByApi!!.profileUrl)
        }

        else {
            Log.e(TAG, "api 로드 실패. spf 시도")
            val spf = requireActivity().getSharedPreferences(
                "momenty_prefs",
                android.content.Context.MODE_PRIVATE
            )

            val user_name = spf.getString("user_name", "사용자")
            val user_profile_image_key = spf.getString("user_profile_image_key", null)
            val user_profile_image_uri = spf.getString("user_profile_image_uri", null)

            if (user_profile_image_key.isNullOrEmpty()) {
                Log.e(TAG, "imagekey is null or empty")
            } else {
                Log.e(TAG, "imagekey: "+user_profile_image_key!!)
            }

            setUserProfile(user_name!!, user_profile_image_key, user_profile_image_uri)
            setPetProfileBySpf()
        }


        rvAdapter.notifyDataSetChanged()
    }

    private fun setRVA() {
        rvAdapter = MyPageRVA(petProfileDatas) {
            clickedItem -> showBottomSheet(clickedItem)
        }
        binding.rvMyPagePetProfile.adapter = rvAdapter
        binding.rvMyPagePetProfile.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
    }

    private fun showBottomSheet(data: MyPagePetProfileData) {
        val bottomSheet = PetSelectBottomSheet()
        bottomSheet.show(childFragmentManager, "PetSelectBottomSheet")
    }

    private fun setUserProfile(name: String, url: String?, uri: String ?= null) {
        binding.tvMyPageUserNickname.text = name
        if (!url.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(url)
                .circleCrop()
                .into(binding.ivMyPageUserProfile)
        } else if (!uri.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(Uri.parse(uri))
                .circleCrop()
                .into(binding.ivMyPageUserProfile)
        }
    }

    private fun setPetProfileBySpf() {
        val spf = requireActivity().getSharedPreferences(
            "momenty_prefs",
            android.content.Context.MODE_PRIVATE
        )
        // 첫 번째 반려동물 데이터
        petProfileDatas.apply {
            clear()
            add(MyPagePetProfileData(
                name = spf.getString("pet_name", "반려동물이름")!!,
                imageKey = spf.getString("pet_profile_image_key", null),
                imageUri = spf.getString("pet_profile_image_uri", null)
            ))
        }

        // 두 번째 이상 반려동물 데이터
        val gson = Gson()
        val petIndex = spf.getLong("pet_index", 0L)
        Log.e("petIndex", petIndex.toString())
        if (petIndex > 0L) {
            for (i in 1L until petIndex+1L) {
                val petData = gson.fromJson(spf.getString("pet_info_${i}", null), MyPagePetProfileData::class.java)
                petProfileDatas.add(petData)
            }
        }

        for (i in 0 until petProfileDatas.size) {
            Log.e("MyPage", "$i petdata:"+petProfileDatas[i].toString())
        }
    }

    private fun setPetProfileByApi(dataList: ArrayList<PetProfileData>) {
        petProfileDatas.clear()
        for (data in dataList) {
            petProfileDatas.apply {
                add(MyPagePetProfileData(
                    name = data.petName,
                    imageKey = data.profileImageUrl
                ))
            }
        }
    }


    private fun performLoadProfile() {
        myPageViewModel.loadProfile()
    }

    private fun observePerformLoadProfile() {
        myPageViewModel.loadProfileResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(requireActivity(), "프로필 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                loadProfileByApi = data
                setName()
                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "프로필 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "프로필 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun performLogout() {
        myPageViewModel.logout()
    }

    private fun observePerformLogout() {
        myPageViewModel.logoutResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(requireActivity(), "로그아웃 성공!", Toast.LENGTH_SHORT).show()
                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(requireActivity(), "로그아웃 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "로그아웃 실패: $message")
                bSuccessApi = false
            }
        }
    }

    override fun onLogoutClickListener() {
        performLogout()
        requireActivity().finish()
    }

    override fun onUnsubscribeClickListener() {
        // TODO: 계정탈퇴
        requireActivity().finish()
    }
}