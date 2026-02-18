package com.example.momenty.domain.member

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.data.api.ImagePickerHelper
import com.example.momenty.databinding.FragmentSignupPetProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class PetProfileFragment : Fragment() {

    private var _binding: FragmentSignupPetProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedPetType: String? = null // 강아지 or 고양이
    private var selectedImageUri: Uri? = null
    private var uploadedImageKey: String? = null  // 변경: URL → Key

    // 갤러리 선택 결과 처리
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

    // 카메라 촬영 결과 처리
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imagePickerHelper?.getTempImageUri()?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

    private var imagePickerHelper: ImagePickerHelper? = null

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

        // ImagePickerHelper 초기화
        imagePickerHelper = ImagePickerHelper(
            requireContext(),
            galleryLauncher,
            cameraLauncher
        )

        // UI 초기화를 다음 프레임으로 연기 (ANR 방지)
        binding.root.post {
            initializeViews()
        }

        observeProfileState()
        observeImageUploadState()
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
            imagePickerHelper?.showImagePickerDialog()
        }

        // ✅ 이름 입력 감지 (2~10자 유효성 검증) - 수정
        binding.etPetName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNameAndUpdateIcon(s.toString())  // ✅ 메서드 호출 변경
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 이름 입력 필드의 아이콘 클릭 처리 - 메서드 변경
        setupNameFieldIconClick()

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

    /**
     * ✅ 이름 유효성 검증 + 아이콘 동적 변경
     * - 입력 없음: 아이콘 숨김
     * - 2~10자: X 버튼 (삭제 가능)
     * - 조건 미달: 경고 아이콘 (클릭 불가)
     */
    private fun validateNameAndUpdateIcon(name: String) {
        val isValid = name.length in 2..10

        binding.etPetName.apply {
            when {
                name.isEmpty() -> {
                    // 입력 없음 - 아이콘 숨김
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    tag = null
                    isActivated = false
                }
                isValid -> {
                    // ✅ 정상 상태 - X 버튼 표시
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.img_profile_cancel, 0
                    )
                    tag = "clear"
                    isActivated = false
                    binding.tvNameErrorMessage.visibility = View.GONE
                }
                else -> {
                    // ❌ 에러 상태 - 경고 아이콘 표시
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.img_profile_warning, 0
                    )
                    tag = "error"
                    isActivated = true  // 빨간 테두리 표시
                    binding.tvNameErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * ✅ 이름 입력 필드의 아이콘 클릭 처리
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupNameFieldIconClick() {
        binding.etPetName.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as android.widget.EditText
                val drawable = editText.compoundDrawablesRelative[2]

                if (drawable != null) {
                    val touchX = event.x.toInt()
                    val drawableWidth = drawable.bounds.width()
                    val drawableStart = editText.width - editText.paddingEnd - drawableWidth

                    if (touchX >= drawableStart) {
                        handleNameIconClick()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    /**
     * ✅ 이름 필드 아이콘 클릭 처리
     */
    private fun handleNameIconClick() {
        when (binding.etPetName.tag) {
            "clear" -> {
                // ✅ X 버튼 클릭 - 텍스트 삭제
                binding.etPetName.text?.clear()
                binding.etPetName.requestFocus()
            }
            "error" -> {
                // ❌ 경고 아이콘 클릭 - 아무 동작 안 함
            }
        }
    }

    /**
     * 이미지 선택 처리
     */
    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri

        // 이미지 미리보기
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.ivPetProfileImg)

        // Presigned URL 방식으로 이미지 업로드
        uploadImageWithPresignedUrl(uri)
    }

    /**
     * Presigned URL 방식으로 이미지 업로드
     */
    private fun uploadImageWithPresignedUrl(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.uploadImage(requireContext(), uri)
        }
    }

    /**
     * 이미지 업로드 상태 관찰
     */
    private fun observeImageUploadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.imageUploadState.collect { state ->
                when (state) {
                    is ImageUploadState.Idle -> {
                        // 초기 상태
                    }
                    is ImageUploadState.Loading -> {
                        Toast.makeText(
                            requireContext(),
                            "이미지 업로드 중...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ImageUploadState.Success -> {
                        uploadedImageKey = state.imageKey  // 변경: URL → Key
                        Toast.makeText(
                            requireContext(),
                            "이미지 업로드 완료",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "업로드된 이미지 Key: $uploadedImageKey")
                    }
                    is ImageUploadState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "이미지 업로드 실패: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "이미지 업로드 실패: ${state.message}")
                    }
                }
            }
        }
    }

    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.uiState.collect { state ->
                when (state) {
                    is ProfileUiState.Idle -> {
                        hideLoading()
                    }
                    is ProfileUiState.Loading -> {
                        showLoading()
                    }
                    is ProfileUiState.Success -> {
                        hideLoading()
                        handleProfileSuccess(state)
                    }
                    is ProfileUiState.Error -> {
                        hideLoading()
                        handleProfileError(state.message)
                    }
                }
            }
        }
    }

    private fun handleProfileSuccess(state: ProfileUiState.Success) {

        Toast.makeText(
            requireContext(),
            "프로필이 저장되었습니다!",
            Toast.LENGTH_SHORT
        ).show()

        // 로컬에도 저장 (캐싱용)
        saveProfileLocally()

        // 메인 화면으로 이동
        navigateToHome()
    }

    private fun handleProfileError(message: String) {
        Log.e(TAG, "프로필 저장 실패: $message")
        Toast.makeText(
            requireContext(),
            "프로필 저장 실패: $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {
        binding.btnPetProfileSave.isEnabled = false
        // TODO: 프로그레스바 표시
    }

    private fun hideLoading() {
        binding.btnPetProfileSave.isEnabled = true
        // TODO: 프로그레스바 숨김
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
        val hasName = binding.etPetName.text?.toString()?.length in 2..10
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
     * 프로필 정보 저장 (백엔드 API 호출)
     */
    private fun saveProfile() {
        // 사용자 프로필 정보 가져오기 (UserProfileFragment에서 저장한 것)
        val prefs = requireActivity().getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )

        val userName = prefs.getString("user_name", "") ?: ""
        val userGender = prefs.getString("user_gender", "") ?: ""
        val userBirth = prefs.getString("user_birth", "") ?: ""
        val alarmTime = prefs.getString("alarm_time", null)
        val userProfileImageKey = prefs.getString("user_profile_image_key", null)  // 변경

        val petName = binding.etPetName.text.toString()
        val petGender = when (binding.rgPetGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val petBirth = binding.etPetBirth.text.toString()
        val petType = binding.etPetType.text.toString()
        val petTypeDetail = binding.etPetTypeDetail.text.toString()
        val petIntro = binding.etPetIntro.text.toString()

        // 날짜 형식 변환 (YY.MM.DD -> YYYY-MM-DD)
        val userBirthFormatted = formatDateForApi(userBirth)
        val petBirthFormatted = formatDateForApi(petBirth)

        // ViewModel을 통해 API 호출 (imageKey 전달)
        profileViewModel.updatePetProfile(
            userName = userName,
            userGender = userGender,
            userBirthDate = userBirthFormatted,
            userImageKey = userProfileImageKey,      // 변경: URL → Key
            alarmTime = alarmTime,
            petName = petName,
            petGender = petGender,
            petBirthDate = petBirthFormatted,
            petImageKey = uploadedImageKey,          // 변경: URL → Key
            petType = petType,
            petBreed = petTypeDetail,
            petIntroduction = petIntro
        )
    }

    /**
     * 날짜 형식 변환 (YY.MM.DD -> YYYY-MM-DD)
     */
    private fun formatDateForApi(date: String): String {
        return try {
            val parts = date.split(".")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val fullYear = if (year < 50) 2000 + year else 1900 + year
                String.format("%04d-%02d-%02d", fullYear, parts[1].toInt(), parts[2].toInt())
            } else {
                date
            }
        } catch (e: Exception) {
            Log.e(TAG, "Date format error: $date", e)
            date
        }
    }

    /**
     * 로컬 저장 (캐싱용)
     */
    private fun saveProfileLocally() {
        val prefs = requireActivity().getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )

        prefs.edit().apply {
            putString("pet_name", binding.etPetName.text.toString())
            putString("pet_gender", when (binding.rgPetGender.checkedRadioButtonId) {
                R.id.rb_gender_male -> "male"
                R.id.rb_gender_female -> "female"
                else -> ""
            })
            putString("pet_birth", binding.etPetBirth.text.toString())
            putString("pet_type", binding.etPetType.text.toString())
            putString("pet_type_detail", binding.etPetTypeDetail.text?.toString())
            putString("pet_intro", binding.etPetIntro.text?.toString())
            putString("pet_profile_image_key", uploadedImageKey)  // 변경: URL → Key
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
        profileViewModel.resetState()
        _binding = null
    }

    companion object {
        private const val TAG = "PetProfileFragment"
    }
}