package com.example.momenty.domain.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.BottomSheetPetSelectBinding
import com.example.momenty.domain.mypage.RVA.PetSelectBottomSheetRVA
import com.example.momenty.domain.mypage.data.MyPagePetProfileData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson

class PetSelectBottomSheet(private val petListData: ArrayList<LoadPetListData>): BottomSheetDialogFragment() {
    lateinit var binding: BottomSheetPetSelectBinding
    //private val petDatas = ArrayList<LoadPetListData>()
    private var petDatas = ArrayList<LoadPetListData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetPetSelectBinding.inflate(inflater, container, false)


        getPetDatas()
        setRVA()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // TODO: 바텀시트 내부 버튼 클릭 이벤트 처리
    }

    private fun initListener() {

    }
    private fun getPetDatas() {
        petDatas = petListData
        /*
        val spf = requireActivity().getSharedPreferences(
            "momenty_prefs",
            android.content.Context.MODE_PRIVATE
        )

        petDatas.clear()
        // 첫 번째 반려동물 데이터
        petDatas.add(MyPagePetProfileData(
            name = spf.getString("pet_name", "반려동물이름")!!,
            imageKey = spf.getString("pet_profile_image_key", null),
            imageUri = spf.getString("pet_profile_image_uri", null)
        ))

        // 두 번째 이상 반려동물 데이터
        val gson = Gson()
        val petIndex = spf.getLong("pet_index", 0L)
        if (petIndex > 0L) {
            for (i in 1L until petIndex+1L) {
                val petData = gson.fromJson(spf.getString("pet_info_${i}", null), MyPagePetProfileData::class.java)
                petDatas.add(petData)
            }
        }*/

        binding.rvPetSelect.adapter?.notifyDataSetChanged()
    }

    private fun setRVA() {
        val RVAdapter = PetSelectBottomSheetRVA(petDatas) { dismiss() }
        binding.rvPetSelect.adapter = RVAdapter
        binding.rvPetSelect.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.VERTICAL, false
        )
    }
}