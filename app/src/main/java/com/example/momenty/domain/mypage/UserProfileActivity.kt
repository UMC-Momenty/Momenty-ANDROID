package com.example.momenty.domain.mypage

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.data.api.ImagePickerHelper
import com.example.momenty.databinding.ActivityUserProfileBinding
import com.example.momenty.domain.home.KhgApiClient
import com.example.momenty.domain.member.ImageUploadState
import com.example.momenty.domain.member.ProfileUiState
import com.example.momenty.domain.member.ProfileViewModel
import com.example.momenty.global.security.TokenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.getValue

@AndroidEntryPoint
class UserProfileActivity: AppCompatActivity() {
    lateinit var binding: ActivityUserProfileBinding
    lateinit var tokenManager: TokenManager
    private val tmpUserId = 1 //TODO: 테스트용. 이후 삭제 바람!!!!
    private var bSuccessApi = false

    private val profileViewModel: ProfileViewModel by viewModels()
    private var isUpdating = false
    private var loadProfileByApi: LoadProfileData ?= null

    private var selectedImageUri: Uri? = null
    private var uploadedImageKey: String? = null
    private val TAG = "UserProfileActivity"

    private val myPageViewModel: MyPageViewModel by viewModels {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val service: MyPageService = KhgApiClient.myPageService
                val repository = MyPageRepository(service)
                return MyPageViewModel(repository) as T
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelected(uri)
            }
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        tokenManager = TokenManager(this)

        setContentView(binding.root)

        imagePickerHelper = ImagePickerHelper(
            this,
            galleryLauncher,
            cameraLauncher
        )

        observePerformUpdateUserProfile()
        observePerformLoadProfile()

        performLoadProfile()
        initializeViews()

        observeProfileState()
        observeImageUploadState()
    }

    private fun initializeViews() {
        setupSpinner()
        initListener()
        getUserProfile()
        updateSaveButton()
    }

    private fun setupSpinner() {
        val timeOptions = resources.getStringArray(R.array.alarm_time_options)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            timeOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = adapter
    }
    private fun initListener() {
        binding.btnUserProfileBack.setOnClickListener {
            finish()
        }

        // 이름 입력 감지
        binding.etUserProfileName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드 clear 버튼
        setupClearButton(binding.etUserProfileName)

        // 프로필 이미지 변경
        binding.layoutPetFormAddPhoto.setOnClickListener {
            imagePickerHelper?.showImagePickerDialog()
        }

        // 성별 선택
        binding.rgUserGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etUserProfileBirthday.setOnClickListener {
            showDatePicker()
        }

        binding.spinnerType.onItemSelectedListener =
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
                        binding.cbUserProfileNoSetAlarm.isChecked = false
                        isUpdating = false
                    }
                    updateSaveButton()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // 알람 설정 안 함 체크박스
        binding.cbUserProfileNoSetAlarm.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener

            if (isChecked) {
                isUpdating = true
                binding.spinnerType.setSelection(0)
                binding.spinnerType.isEnabled = false
                isUpdating = false
            } else {
                binding.spinnerType.isEnabled = true
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
            this,
            { _, year, month, dayOfMonth ->
                // YY.MM.DD 형식으로 포맷
                val formattedDate = String.format(
                    "%02d.%02d.%02d",
                    year % 100,
                    month + 1,
                    dayOfMonth
                )
                binding.etUserProfileBirthday.setText(formattedDate)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClearButton(editText: android.widget.EditText) {
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]

                if (drawableEnd != null) {
                    val touchX = event.x.toInt()
                    val drawableWidth = drawableEnd.intrinsicWidth
                    val drawableStart = editText.width - editText.paddingEnd - drawableWidth

                    if (touchX >= drawableStart) {
                        editText.text?.clear()
                        return@setOnTouchListener true
                    }
                }
            }
            false
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
            .into(binding.ivPetFormPetProfileEdit)

        // Presigned URL 방식으로 이미지 업로드
        uploadImageWithPresignedUrl(uri)
    }

    /**
     * Presigned URL 방식으로 이미지 업로드
     */
    private fun uploadImageWithPresignedUrl(uri: Uri) {
        lifecycleScope.launch {
            profileViewModel.uploadImage(this@UserProfileActivity, uri)
        }
    }

    /**
     * 이미지 업로드 상태 관찰
     */
    private fun observeImageUploadState() {
        lifecycleScope.launch {
            profileViewModel.imageUploadState.collect { state ->
                when (state) {
                    is ImageUploadState.Idle -> {
                        // 초기 상태
                    }
                    is ImageUploadState.Loading -> {
                        Toast.makeText(
                            this@UserProfileActivity,
                            "이미지 업로드 중...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ImageUploadState.Success -> {
                        uploadedImageKey = state.imageKey  // 변경: URL → Key
                        Toast.makeText(
                            this@UserProfileActivity,
                            "이미지 업로드 완료",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "업로드된 이미지 Key: $uploadedImageKey")
                    }
                    is ImageUploadState.Error -> {
                        Toast.makeText(
                            this@UserProfileActivity,
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
        lifecycleScope.launch {
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
        Log.d(TAG, "프로필 저장 성공: userId=${state.userId}, petId=${state.petId}")

        Toast.makeText(
            this@UserProfileActivity,
            "프로필이 저장되었습니다!",
            Toast.LENGTH_SHORT
        ).show()

        // 로컬에도 저장 (캐싱용)
        //saveProfileLocally() TODO: 원래 있었음

        finish()
    }

    private fun handleProfileError(message: String) {
        Log.e(TAG, "프로필 저장 실패: $message")
        Toast.makeText(
            this@UserProfileActivity,
            "프로필 저장 실패: $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {
        binding.btnUserProfileSave.isEnabled = false
        // TODO: 프로그레스바 표시
    }

    private fun hideLoading() {
        binding.btnUserProfileSave.isEnabled = true
        // TODO: 프로그레스바 숨김
    }

    private fun updateSaveButton() {
        val hasName = binding.etUserProfileName.text?.isNotBlank() == true
        val hasGender = binding.rgUserGender.checkedRadioButtonId != -1
        val hasBirth = binding.etUserProfileBirthday.text?.isNotBlank() == true
        val hasAlarmSetting = binding.spinnerType.selectedItemPosition > 0
                || binding.cbUserProfileNoSetAlarm.isChecked

        val allFieldsFilled = hasName && hasGender && hasBirth && hasAlarmSetting

        binding.btnUserProfileSave.isEnabled = allFieldsFilled

        // 버튼 텍스트 색상 변경
        if (allFieldsFilled) {
            binding.btnUserProfileSave.setTextColor(
                ContextCompat.getColor(this, R.color.white)
            )
        } else {
            binding.btnUserProfileSave.setTextColor(
                ContextCompat.getColor(this, R.color.caption_3)
            )
        }
    }

    private fun saveProfile() {
        val name = binding.etUserProfileName.text.toString()
        val gender = when (binding.rgUserGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val birth = binding.etUserProfileBirthday.text.toString()

        val alarmTime = if (binding.cbUserProfileNoSetAlarm.isChecked) {
            null
        } else {
            binding.spinnerType.selectedItem as? String
        }

        performUpdateUserProfile() //TODO: 백엔드 서버 열리면 다시 켜서 확인해볼것!!!
        // SharedPreferences에 저장
        if (name == null) Log.e(TAG, "name is null")
        if (gender == null) Log.e(TAG, "gender is null")
        if (birth == null) Log.e(TAG, "birth is null")
        if (alarmTime == null) Log.e(TAG, "alarmTime is null")
        if (uploadedImageKey == null) Log.e(TAG, "uploadedImageKey is null")
        if (selectedImageUri == null) Log.e(TAG, "selectedImageUri is null")
        saveToPreferences(name, gender, birth, alarmTime, uploadedImageKey, selectedImageUri)


        finish()
    }

    private fun getUserProfile() {
        if (bSuccessApi) {
            TODO("api 데이터 연결")
            binding.etUserProfileName.setText(loadProfileByApi?.username)

            val parsedDate = ZonedDateTime.parse(loadProfileByApi?.birth)

            // 연도 2자리(yy), 월 2자리(MM), 일 2자리(dd)로 포맷터 생성
            val formatter = DateTimeFormatter.ofPattern("yy-MM-dd")
            val result = parsedDate.format(formatter)
            binding.etUserProfileBirthday.setText(result)

            when (loadProfileByApi?.gender) {
                "male" -> binding.rgUserGender.check(R.id.rb_gender_male)
                else -> binding.rgUserGender.check(R.id.rb_gender_female)
            }

            if (loadProfileByApi?.questTime == null) {
                binding.cbUserProfileNoSetAlarm.isChecked = true
            } else {
                val toIndex = getSpinnerIndex(binding.spinnerType, loadProfileByApi?.questTime!!)
                binding.spinnerType.setSelection(toIndex)
            }

            if (!loadProfileByApi?.profileUrl.isNullOrEmpty()) {
                //val baseUrl = "https://api.momenty.com/"
                //val imageUrl = baseUrl + user_profile_image_key
                selectedImageUri = loadProfileByApi?.profileUrl?.toUri()

                Glide.with(binding.root.context)
                    .load(loadProfileByApi?.profileUrl)
                    .circleCrop()
                    .into(binding.ivPetFormPetProfileEdit)
            }
        } else {
            val spf = getSharedPreferences(
                "momenty_prefs",
                android.content.Context.MODE_PRIVATE
            )
            val user_name = spf.getString("user_name", "사용자")
            val user_gender = spf.getString("user_gender", "male")
            val user_birth = spf.getString("user_birth", "00.01.01")
            val alarm_time: String ?= spf.getString("alarm_time", null)
            val user_profile_image_key = spf.getString("user_profile_image_key", null)
            val user_profile_image_uri = spf.getString("user_profile_image_uri", null)

            Log.d("gender", user_gender!!)

            binding.etUserProfileName.setText(user_name)
            binding.etUserProfileBirthday.setText(user_birth)

            when (user_gender) {
                "male" -> binding.rgUserGender.check(R.id.rb_gender_male)
                else -> binding.rgUserGender.check(R.id.rb_gender_female)
            }
            if (alarm_time == null) {
                binding.cbUserProfileNoSetAlarm.isChecked = true
            } else {
                val toIndex = getSpinnerIndex(binding.spinnerType, alarm_time)
                binding.spinnerType.setSelection(toIndex)
            }

            if (!user_profile_image_key.isNullOrEmpty()) {
                //val baseUrl = "https://api.momenty.com/"
                //val imageUrl = baseUrl + user_profile_image_key
                selectedImageUri = user_profile_image_key.toUri()

                Glide.with(binding.root.context)
                    .load(user_profile_image_key)
                    .circleCrop()
                    .into(binding.ivPetFormPetProfileEdit)
            } else if (!user_profile_image_uri.isNullOrEmpty()) {
                selectedImageUri = user_profile_image_uri.toUri()
                Glide.with(binding.root.context)
                    .load(Uri.parse(user_profile_image_uri))
                    .circleCrop()
                    .into(binding.ivPetFormPetProfileEdit)
            }
        }
    }

    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        alarmTime: String?,
        profileImageKey: String?,
        uri: Uri?
    ) {
        val prefs = getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )

        prefs.edit().apply {
            putString("user_name", name)
            putString("user_gender", gender)
            putString("user_birth", birth)
            putString("alarm_time", alarmTime)
            putString("user_profile_image_key", profileImageKey)
            putString("user_profile_image_uri", uri.toString())
            putBoolean("profile_completed", true)
            apply()
        }
    }

    private fun getSpinnerIndex(sp: Spinner, item: String): Int{
        for (i in 0..sp.count-1) {
            if (sp.getItemAtPosition(i).toString() == item) {
                return i
            }
        }
        return 0
    }

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

    private fun performUpdateUserProfile() {
        val alarmTime = if (binding.cbUserProfileNoSetAlarm.isChecked) {
            null
        } else {
            binding.spinnerType.selectedItem as? String
        }

        val req = UpdateUserProfileRequest(
            binding.etUserProfileName.text.toString(),
            uploadedImageKey,
            when (binding.rgUserGender.checkedRadioButtonId) {
                R.id.rb_gender_male -> "male"
                R.id.rb_gender_female -> "female"
                else -> ""
            },
            formatDateForApi(binding.etUserProfileBirthday.text.toString()),
            alarmTime
        )

        val accessToken = tokenManager.getAccessToken()
        val userId = tokenManager.getUserId()
        myPageViewModel.updateUserProfile(accessToken!!, userId, req)
    }

    private fun observePerformUpdateUserProfile() {
        myPageViewModel.updateUserProfileResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(this, "프로필 수정 성공!", Toast.LENGTH_SHORT).show()
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "프로필 수정 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "프로필 수정 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun performLoadProfile() {
        val accessToken = tokenManager.getAccessToken()
        val userId = tokenManager.getUserId()
        myPageViewModel.loadProfile(accessToken!!, userId)
    }

    private fun observePerformLoadProfile() {
        myPageViewModel.loadProfileResult.observe(this) { result ->
            result.onSuccess { data ->
                Toast.makeText(this, "프로필 로드 성공!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "작성 데이터: $data")
                loadProfileByApi = data
                bSuccessApi = true
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "프로필 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "프로필 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }
}