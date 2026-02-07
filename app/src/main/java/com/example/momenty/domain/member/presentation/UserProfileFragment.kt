package com.example.momenty.domain.member.presentation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.momenty.R
import com.example.momenty.databinding.FragmentSignupUserProfileBinding
import java.util.Calendar

class UserProfileFragment : Fragment() {

    private var _binding: FragmentSignupUserProfileBinding? = null
    private val binding get() = _binding!!

    private var isUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupUserProfileBinding.inflate(inflater, container, false)
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
        setupSpinner()
        setupListeners()
        updateSaveButton()
    }

    private fun setupSpinner() {
        val timeOptions = resources.getStringArray(R.array.alarm_time_options)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuestionAlarm.adapter = adapter
    }

    private fun setupListeners() {
        // 뒤로가기
        binding.ivArrowBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // 프로필 이미지 변경
        binding.ivUserProfileCam.setOnClickListener {
            // TODO: 이미지 선택 다이얼로그
            showImagePickerDialog()
        }

        // 이름 입력 감지
        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드 clear 버튼
        binding.etUserName.setOnFocusChangeListener { _, hasFocus ->
            // EditText에 drawableEnd로 clear 아이콘이 있다면 클릭 이벤트 추가 가능
        }

        // 성별 선택
        binding.rgUserGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etUserBirth.setOnClickListener {
            showDatePicker()
        }

        // 알람 시간 스피너
        binding.spinnerQuestionAlarm.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (isUpdating) return

                    // "option"이 아닌 실제 시간 선택 시
                    if (position > 0) {
                        isUpdating = true
                        binding.cbNoAlarm.isChecked = false
                        isUpdating = false
                    }
                    updateSaveButton()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // 알람 설정 안 함 체크박스
        binding.cbNoAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener

            if (isChecked) {
                isUpdating = true
                binding.spinnerQuestionAlarm.setSelection(0)
                binding.spinnerQuestionAlarm.isEnabled = false
                isUpdating = false
            } else {
                binding.spinnerQuestionAlarm.isEnabled = true
            }
            updateSaveButton()
        }

        // 저장 버튼
        binding.btnUserProfileSave.setOnClickListener {
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
                binding.etUserBirth.setText(formattedDate)
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
        android.widget.Toast.makeText(
            requireContext(),
            "프로필 이미지 선택 기능 구현 예정",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하여 저장 버튼 활성화/비활성화
     */
    private fun updateSaveButton() {
        val hasName = binding.etUserName.text?.isNotBlank() == true
        val hasGender = binding.rgUserGender.checkedRadioButtonId != -1
        val hasBirth = binding.etUserBirth.text?.isNotBlank() == true
        val hasAlarmSetting = binding.spinnerQuestionAlarm.selectedItemPosition > 0
                || binding.cbNoAlarm.isChecked

        val allFieldsFilled = hasName && hasGender && hasBirth && hasAlarmSetting

        binding.btnUserProfileSave.isEnabled = allFieldsFilled

        // 버튼 텍스트 색상 변경
        if (allFieldsFilled) {
            binding.btnUserProfileSave.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        } else {
            binding.btnUserProfileSave.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caption_3)
            )
        }
    }

    /**
     * 프로필 정보 저장
     */
    private fun saveProfile() {
        val name = binding.etUserName.text.toString()
        val gender = when (binding.rgUserGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val birth = binding.etUserBirth.text.toString()

        val alarmTime = if (binding.cbNoAlarm.isChecked) {
            null
        } else {
            binding.spinnerQuestionAlarm.selectedItem as? String
        }

        // SharedPreferences에 저장
        saveToPreferences(name, gender, birth, alarmTime)

        // 메인 화면으로 이동
        navigateToHome()
    }

    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        alarmTime: String?
    ) {
        val prefs = requireActivity().getSharedPreferences(
            "momenty_prefs",
            android.content.Context.MODE_PRIVATE
        )

        prefs.edit().apply {
            putString("user_name", name)
            putString("user_gender", gender)
            putString("user_birth", birth)
            putString("alarm_time", alarmTime)
            putBoolean("profile_completed", true)
            apply()
        }
    }

    private fun navigateToHome() {
        // home_graph로 이동 및 백스택 정리
        val navOptions = androidx.navigation.NavOptions.Builder()
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