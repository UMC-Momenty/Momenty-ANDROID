package com.example.momenty.domain.mypage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentMyPageBinding
import com.example.momenty.domain.main.presentation.MainActivity
import com.kakao.sdk.user.model.User

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentMyPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        setName()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_my_page, container, false)
        binding = FragmentMyPageBinding.inflate(layoutInflater)

        initListener()
        setName()

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
        binding.btnMyPageManagePet.setOnClickListener {
            activityTransition(PetFormManageActivity())
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


    }

    private fun activityTransition(act: AppCompatActivity){
        val intent = Intent(requireActivity(), act::class.java)
        startActivity(intent)
    }

    private fun setName() {
        val spf = requireActivity().getSharedPreferences(
            "momenty_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val user_name = spf.getString("user_name", "사용자")
        val pet_name = spf.getString("pet_name", "반려동물이름")
        binding.tvMyPageUserNickname.text = user_name
        binding.tvMyPagePetName.text = pet_name
    }
}