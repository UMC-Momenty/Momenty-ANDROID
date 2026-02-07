package com.example.momenty.domain.member.presentation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentSignupPetProfileBinding
import java.util.Calendar

class PetProfileFragment : Fragment() {

    private var _binding: FragmentSignupPetProfileBinding? = null
    private val binding get() = _binding!!

    private var selectedPetType: String? = null // 강아지 or 고양이

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupPetProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 초기화를 다음 프레임으로 연기 (ANR 방지)
        binding.root.post {
            initializeViews()
        }
    }

    private fun initializeViews() {
        setupListeners()
        updateSaveButton()
    }

    private fun setupListeners() {
        // 뒤로가기
        binding.ivArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 프로필 이미지 변경
        binding.ivPetProfileCam.setOnClickListener {
            // TODO: 이미지 선택 다이얼로그
            showImagePickerDialog()
        }

        // 이름 입력 감지
        binding.etPetName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드 clear 버튼
        binding.etPetName.setOnFocusChangeListener { _, hasFocus ->
            // EditText에 drawableEnd로 clear 아이콘이 있다면 클릭 이벤트 추가 가능
        }

        // 성별 선택
        binding.rgPetGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etPetBirth.setOnClickListener {
            showDatePicker()
        }

        // 종 선택 (강아지/고양이)
        binding.etPetType.setOnClickListener {
            showPetTypeDialog()
        }
        binding.etPetType.isFocusable = false
        binding.etPetType.isClickable = true

        // 품종 선택
        binding.etPetTypeDetail.setOnClickListener {
            if (selectedPetType != null) {
                showTypeBreedDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "먼저 종을 선택해주세요",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.etPetTypeDetail.isFocusable = false
        binding.etPetTypeDetail.isClickable = true

        // 소개 입력 감지
        binding.etPetIntro.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 저장 버튼
        binding.btnPetProfileSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // YY.MM.DD 형식으로 포맷
                val formattedDate = String.format(
                    "%02d.%02d.%02d",
                    year % 100,
                    month + 1,
                    dayOfMonth
                )
                binding.etPetBirth.setText(formattedDate)
                updateSaveButton()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 최대 날짜를 오늘로 설정 (미래 날짜 선택 방지)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    private fun showImagePickerDialog() {
        // TODO: 이미지 선택 기능 구현
        // - 갤러리에서 선택
        // - 카메라로 촬영
        // 임시로 토스트 메시지
        Toast.makeText(
            requireContext(),
            "프로필 이미지 선택 기능 구현 예정",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPetTypeDialog() {
        val petTypes = arrayOf("강아지", "고양이")

        AlertDialog.Builder(requireContext())
            .setTitle("종 선택")
            .setItems(petTypes) {dialog, which ->
                selectedPetType = petTypes[which]
                binding.etPetType.setText(selectedPetType)

                // 종 변경 시 품종 초기화
                binding.etPetTypeDetail.setText("")

                updateSaveButton()
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTypeBreedDialog()  {
        val breeds = when(selectedPetType)  {
            "강아지" -> resources.getStringArray(R.array.dog_type_options)
            "고양이" -> resources.getStringArray(R.array.cat_type_options)
            else -> return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("품종 선택")
            .setItems(breeds)   { dialog, which ->
                binding.etPetTypeDetail.setText(breeds[which])
                updateSaveButton()
                dialog.dismiss()
            }
            .setNegativeButton("취소")    {dialog, _ ->
                // 공백 설정
                binding.etPetTypeDetail.setText("")
                updateSaveButton()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하여 저장 버튼 활성화/비활성화
     */
    private fun updateSaveButton() {
        val hasName = binding.etPetName.text?.isNotBlank() == true
        val hasGender = binding.rgPetGender.checkedRadioButtonId != -1
        val hasBirth = binding.etPetBirth.text?.isNotBlank() == true
        val hasType = binding.etPetType.text?.isNotBlank() == true
        val allFieldsFilled = hasName && hasGender && hasBirth && hasType

        binding.btnPetProfileSave.isEnabled = allFieldsFilled

        // 버튼 텍스트 색상 변경
        if (allFieldsFilled) {
            binding.btnPetProfileSave.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        } else {
            binding.btnPetProfileSave.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caption_3)
            )
        }
    }

    /**
     * 프로필 정보 저장
     */
    private fun saveProfile() {
        val name = binding.etPetName.text.toString()
        val gender = when (binding.rgPetGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val birth = binding.etPetBirth.text.toString()
        val type = binding.etPetType.text.toString()
        val typeDetail = binding.etPetTypeDetail.text.toString()
        val intro = binding.etPetIntro.text.toString()


        // SharedPreferences에 저장
        saveToPreferences(name, gender, birth, type, typeDetail, intro)

        // 메인 화면으로 이동
        navigateToHome()
    }

    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        type: String,
        typeDetail: String,
        intro: String
    ) {
        val prefs = requireActivity().getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )

        prefs.edit().apply {
            putString("pet_name", name)
            putString("pet_gender", gender)
            putString("pet_birth", birth)
            putString("pet_type", type)
            putString("pet_type_detail", typeDetail)
            putString("pet_intro", intro)
            putBoolean("pet_profile_completed", true)
            apply()
        }
    }

    private fun navigateToHome() {
        // home_graph로 이동 및 백스택 정리
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.auth_graph, true)
            .setLaunchSingleTop(true)
            .build()

        findNavController().navigate(R.id.home_graph, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}