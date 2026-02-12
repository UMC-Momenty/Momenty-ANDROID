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

        // 프로필 이미지 변경 (권한 확인 후 이미지 선택)
        binding.ivUserProfileCam.setOnClickListener {
            requestImagePermissions()
        }

        // 이름 입력 감지
        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드의 drawableEnd (X 버튼) 클릭 처리
        setupClearButton(binding.etUserName)

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

                    if (position > 0) {
                        isUpdating = true
                        binding.cbNoAlarm.isChecked = false
                        isUpdating = false
                    } else {
                        isUpdating = true
                        binding.spinnerQuestionAlarm.setSelection(0)
                        binding.spinnerQuestionAlarm.isEnabled = false
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

    /**
     * EditText의 drawableEnd (Clear 버튼) 클릭 처리
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearButton(editText: android.widget.EditText) {
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // drawableEnd (오른쪽 아이콘) 영역인지 확인
                val drawableEnd = editText.compoundDrawables[2] // 0:left, 1:top, 2:right, 3:bottom

                if (drawableEnd != null) {
                    // 터치 위치가 drawableEnd 영역인지 확인
                    val touchX = event.x.toInt()
                    val drawableWidth = drawableEnd.intrinsicWidth
                    val drawableStart = editText.width - editText.paddingEnd - drawableWidth

                    if (touchX >= drawableStart) {
                        // Clear 버튼 클릭됨 → EditText 내용 지우기
                        editText.text?.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
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
        val hasName = binding.etUserName.text?.isNotBlank() == true
        val hasGender = binding.rgUserGender.checkedRadioButtonId != -1
        val hasBirth = binding.etUserBirth.text?.isNotBlank() == true
        val hasAlarmSetting = binding.spinnerQuestionAlarm.selectedItemPosition > 0
                || binding.cbNoAlarm.isChecked

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

        val alarmTime = if (binding.cbNoAlarm.isChecked) {
            null
        } else {
            binding.spinnerQuestionAlarm.selectedItem as? String
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