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
    private var selectedAlarmTime: String? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (PermissionHelper.areAllPermissionsGranted(permissions)) {
            imagePickerHelper?.showImagePickerDialog()
        } else {
            Toast.makeText(requireContext(), "카메라 및 갤러리 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    private var permissionHelper: PermissionHelper? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleImageSelected(uri) }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imagePickerHelper?.getTempImageUri()?.let { uri -> handleImageSelected(uri) }
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

        initializeHelpers()
        binding.root.post { initializeViews() }
        observeImageUploadState()
        observeProfileState()  // 추가
    }

    private fun initializeHelpers() {
        permissionHelper = PermissionHelper(
            fragment = this,
            permissionLauncher = permissionLauncher
        )
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
        binding.ivArrowBack.setOnClickListener { findNavController().navigateUp() }

        binding.ivUserProfileCam.setOnClickListener { requestImagePermissions() }

        binding.etUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateNameAndUpdateIcon(s.toString())
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setupNameFieldIconClick()

        binding.rgUserGender.setOnCheckedChangeListener { _, _ -> updateSaveButton() }

        binding.etUserBirth.setOnClickListener { showDatePicker() }

        binding.etQuestionAlarm.setOnClickListener { showAlarmTimePicker() }

        binding.cbNoAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener
            if (isChecked) {
                selectedAlarmTime = null
                binding.etQuestionAlarm.setText("")
            }
            updateSaveButton()
        }

        binding.btnUserProfileSave.setOnClickListener { saveProfile() }
    }

    private fun validateNameAndUpdateIcon(name: String) {
        val isValid = name.length in 2..10
        binding.etUserName.apply {
            when {
                name.isEmpty() -> {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    tag = null
                    isActivated = false
                }
                isValid -> {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.img_profile_cancel, 0)
                    tag = "clear"
                    isActivated = false
                    binding.tvNameErrorMessage.visibility = View.GONE
                }
                else -> {
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.img_profile_warning, 0)
                    tag = "error"
                    isActivated = true
                    binding.tvNameErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupNameFieldIconClick() {
        binding.etUserName.setOnTouchListener { v, event ->
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

    private fun handleNameIconClick() {
        if (binding.etUserName.tag == "clear") {
            binding.etUserName.text?.clear()
            binding.etUserName.requestFocus()
        }
    }

    private fun requestImagePermissions() {
        permissionHelper?.checkAndRequestImagePermissions {
            imagePickerHelper?.showImagePickerDialog()
        }
    }

    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri
        Glide.with(this).load(uri).circleCrop().into(binding.ivUserProfileImg)
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.uploadImage(requireContext(), uri)
        }
    }

    private fun observeImageUploadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.imageUploadState.collect { state ->
                when (state) {
                    is ImageUploadState.Idle -> {}
                    is ImageUploadState.Loading -> {
                        Toast.makeText(requireContext(), "이미지 업로드 중...", Toast.LENGTH_SHORT).show()
                    }
                    is ImageUploadState.Success -> {
                        uploadedImageKey = state.imageKey
                        Log.d(TAG, "업로드된 이미지 Key: $uploadedImageKey")
                    }
                    is ImageUploadState.Error -> {
                        Toast.makeText(requireContext(), "이미지 업로드 실패: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * 프로필 저장 API 응답 관찰
     * 성공 시 ROLE_USER 토큰은 ViewModel에서 저장됨
     */
    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.uiState.collect { state ->
                when (state) {
                    is ProfileUiState.Idle -> hideLoading()
                    is ProfileUiState.Loading -> showLoading()
                    is ProfileUiState.Success -> {
                        hideLoading()
                        saveUserInfoLocally()
                        // 토큰은 ProfileViewModel에서 이미 저장됨
                        navigateToPetProfile()
                    }
                    is ProfileUiState.Error -> {
                        hideLoading()
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "프로필 저장 실패: ${state.message}")
                    }
                }
            }
        }
    }

    private fun saveUserInfoLocally() {
        requireActivity().getSharedPreferences("momenty_prefs", Context.MODE_PRIVATE)
            .edit().apply {
                putString("user_name", binding.etUserName.text.toString())
                putString("user_gender", when (binding.rgUserGender.checkedRadioButtonId) {
                    R.id.rb_gender_male -> "MALE"
                    R.id.rb_gender_female -> "FEMALE"
                    else -> ""
                })
                putString("user_birth", binding.etUserBirth.text.toString())
                putString("alarm_time", selectedAlarmTime)
                putString("user_profile_image_key", uploadedImageKey)
                apply()
            }
    }

    private fun showAlarmTimePicker() {
        val dialog = AlarmTimePickerDialog(requireContext()) { timeString ->
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
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d.%02d.%02d", year % 100, month + 1, dayOfMonth)
                binding.etUserBirth.setText(formattedDate)
                updateSaveButton()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
            show()
        }
    }

    private fun updateSaveButton() {
        val hasName = binding.etUserName.text?.toString()?.length in 2..10
        val hasGender = binding.rgUserGender.checkedRadioButtonId != -1
        val hasBirth = binding.etUserBirth.text?.isNotBlank() == true
        val hasAlarmSetting = selectedAlarmTime != null || binding.cbNoAlarm.isChecked
        val allFieldsFilled = hasName && hasGender && hasBirth && hasAlarmSetting

        binding.btnUserProfileSave.isEnabled = allFieldsFilled
        binding.btnUserProfileSave.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (allFieldsFilled) R.color.white else R.color.caption_3
            )
        )
    }

    /**
     * API 호출 - 응답 후 observeProfileState()에서 화면 이동
     */
    private fun saveProfile() {
        val name = binding.etUserName.text.toString()
        val gender = when (binding.rgUserGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "MALE"
            R.id.rb_gender_female -> "FEMALE"
            else -> ""
        }
        val birth = convertDateFormat(binding.etUserBirth.text.toString())
        val resetQuestTime = binding.cbNoAlarm.isChecked

        profileViewModel.updateUserProfile(
            username = name,
            gender = gender,
            birth = birth,
            profileUrl = uploadedImageKey,
            questTime = selectedAlarmTime,
            resetQuestTime = if (resetQuestTime) true else null
        )
        // ❌ saveToPreferences(), navigateToPetProfile() 직접 호출 제거
        // ✅ API 응답은 observeProfileState()에서 처리
    }

    private fun convertDateFormat(date: String): String {
        return try {
            val parts = date.split(".")
            "20${parts[0]}-${parts[1]}-${parts[2]}"
        } catch (e: Exception) {
            date
        }
    }

    private fun showLoading() {
        binding.btnUserProfileSave.isEnabled = false
    }

    private fun hideLoading() {
        updateSaveButton()
    }

    private fun navigateToPetProfile() {
        findNavController().navigate(R.id.action_userProfileFragment_to_petProfileFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileViewModel.resetState()
        _binding = null
    }

    companion object {
        private const val TAG = "UserProfileFragment"
    }
}