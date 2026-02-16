package com.example.momenty.domain.member

import android.annotation.SuppressLint
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.data.api.ImagePickerHelper
import com.example.momenty.data.api.PermissionHelper
import com.example.momenty.databinding.FragmentSignupUserProfileBinding
import com.example.momenty.domain.calendar.AlarmTimePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private var _binding: FragmentSignupUserProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    private var isUpdating = false

    private var selectedImageUri: Uri? = null
    private var uploadedImageKey: String? = null

    // 선택된 알람 시간 (null이면 알람 설정 안 함)
    private var selectedAlarmTime: String? = null

    // ===== 권한 관련 =====

    // 권한 요청 결과 처리
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (PermissionHelper.areAllPermissionsGranted(permissions)) {
            // 권한 승인됨 → 이미지 선택 다이얼로그 표시
            imagePickerHelper?.showImagePickerDialog()
        } else {
            // 권한 거부됨
            Toast.makeText(
                requireContext(),
                "카메라 및 갤러리 권한이 필요합니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private var permissionHelper: PermissionHelper? = null

    // ===== 이미지 선택 관련 =====

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
        _binding = FragmentSignupUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Helper 초기화
        initializeHelpers()

        // UI 초기화를 다음 프레임으로 연기 (ANR 방지)
        binding.root.post {
            initializeViews()
        }

        observeImageUploadState()
    }

    private fun initializeHelpers() {
        // PermissionHelper 초기화
        permissionHelper = PermissionHelper(
            fragment = this,
            permissionLauncher = permissionLauncher
        )

        // ImagePickerHelper 초기화
        imagePickerHelper = ImagePickerHelper(
            requireContext(),
            galleryLauncher,
            cameraLauncher
        )
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

        // 프로필 이미지 변경 (권한 확인 후 이미지 선택)
        binding.ivUserProfileCam.setOnClickListener {
            requestImagePermissions()
        }

        // 이름 입력 감지
        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNameAndUpdateIcon(s.toString())
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드의 drawableEnd (X 버튼) 클릭 처리
        setupNameFieldIconClick()

        // 성별 선택
        binding.rgUserGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etUserBirth.setOnClickListener {
            showDatePicker()
        }

        // 알람 시간
        binding.etQuestionAlarm.setOnClickListener { showAlarmTimePicker() }

        // 알람 설정 안 함 체크박스
        binding.cbNoAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener

            if (isChecked) {
                // 체크하면 null로 설정 (하지만 EditText 클릭은 여전히 가능)
                selectedAlarmTime = null
                binding.etQuestionAlarm.setText("")
            }
            updateSaveButton()
        }

        // 저장 버튼
        binding.btnUserProfileSave.setOnClickListener {
            saveProfile()
        }
    }

    /**
     * 이름 유효성 검증 (2~10자)
     */
    private fun validateNameAndUpdateIcon(name: String) {
        val isValid = name.length in 2..10

        binding.etUserName.apply {
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
     * EditText의 drawableEnd (Clear 버튼) 클릭 처리
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupNameFieldIconClick() {
        binding.etUserName.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val editText = v as android.widget.EditText
                val drawable = editText.compoundDrawablesRelative[2] // drawableEnd

                if (drawable != null) {
                    // 터치 위치가 아이콘 영역인지 확인
                    val touchX = event.x.toInt()
                    val drawableWidth = drawable.bounds.width()
                    val drawableStart = editText.width - editText.paddingEnd - drawableWidth

                    if (touchX >= drawableStart) {
                        // ✅ 아이콘 클릭됨 - tag에 따라 다르게 동작
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
        when (binding.etUserName.tag) {
            "clear" -> {
                // ✅ X 버튼 클릭 - 텍스트 삭제
                binding.etUserName.text?.clear()
                binding.etUserName.requestFocus()
            }
        }
    }

    /**
     * 이미지 권한 요청
     */
    private fun requestImagePermissions() {
        permissionHelper?.checkAndRequestImagePermissions {
            // 권한이 이미 허용되어 있음 → 이미지 선택 다이얼로그 표시
            imagePickerHelper?.showImagePickerDialog()
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
            .into(binding.ivUserProfileImg)

        // Presigned URL 방식으로 이미지 업로드
        uploadImageWithPresignedUrl(uri)
    }

    /**
     * 이미지 업로드
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
                        uploadedImageKey = state.imageKey
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

    /**
     * 커스텀 알람 시간 선택 다이얼로그 표시
     */
    private fun showAlarmTimePicker() {
        val dialog = AlarmTimePickerDialog(requireContext()) { timeString ->
            // 시간이 선택되면 EditText에 표시하고 체크박스 해제
            selectedAlarmTime = timeString
            binding.etQuestionAlarm.setText(timeString)

            isUpdating = true
            binding.cbNoAlarm.isChecked = false
            isUpdating = false

            updateSaveButton()
        }
        dialog.show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
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

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateSaveButton() {
        val hasName = binding.etUserName.text?.toString()?.length in 2..10
        val hasGender = binding.rgUserGender.checkedRadioButtonId != -1
        val hasBirth = binding.etUserBirth.text?.isNotBlank() == true
        val hasAlarmSetting = selectedAlarmTime != null || binding.cbNoAlarm.isChecked

        val allFieldsFilled = hasName && hasGender && hasBirth && hasAlarmSetting

        binding.btnUserProfileSave.isEnabled = allFieldsFilled

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

    private fun saveProfile() {
        val name = binding.etUserName.text.toString()
        val gender = when (binding.rgUserGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val birth = binding.etUserBirth.text.toString()

        // 알람 시간: 체크박스가 선택되면 null, 아니면 선택된 시간
        val alarmTime = if (binding.cbNoAlarm.isChecked) {
            null
        } else {
            selectedAlarmTime
        }

        saveToPreferences(name, gender, birth, alarmTime, uploadedImageKey)
        navigateToPetProfile()
    }

    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        alarmTime: String?,
        profileImageKey: String?
    ) {
        val prefs = requireActivity().getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )

        prefs.edit().apply {
            putString("user_name", name)
            putString("user_gender", gender)
            putString("user_birth", birth)
            putString("alarm_time", alarmTime)
            putString("user_profile_image_key", profileImageKey)
            putBoolean("profile_completed", true)
            apply()
        }
    }

    private fun navigateToPetProfile() {
        findNavController().navigate(R.id.action_userProfileFragment_to_petProfileFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "UserProfileFragment"
    }
}