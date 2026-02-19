package com.example.momenty.domain.mypage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.momenty.R
import com.example.momenty.data.api.ImagePickerHelper
import com.example.momenty.databinding.ActivityPetFormManageBinding
import com.example.momenty.domain.member.ImageUploadState
import com.example.momenty.domain.member.ProfileUiState
import com.example.momenty.domain.member.ProfileViewModel
import com.example.momenty.domain.mypage.data.MyPagePetProfileData
import com.example.momenty.global.security.TokenManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class PetFormManageActivity: AppCompatActivity() {
    lateinit var binding: ActivityPetFormManageBinding

    @Inject
    lateinit var tokenManager: TokenManager
    //private val tmpPetId = 1L //TODO: 테스트용. 이후 삭제 바람!!!!
    //private val tmpBreedId: Long? = 1L //TODO: 테스트용. 이후 삭제 바람!!!!
    private var breedIndex: Int ?= null
    private var bSuccessApi = false

    private var loadOnePetProfileDataByApi: LoadOnePetProfileData ?= null

    private val profileViewModel: ProfileViewModel by viewModels()
    private var selectedPetType: String? = null // 강아지 or 고양이
    private var selectedImageUri: Uri? = null
    private var uploadedImageKey: String? = null  // 변경: URL → Key
    private val TAG = "PetManageAct"

    private val myPageViewModel: MyPageViewModel by viewModels {
        val repo = MyPageRepository(
            service = MyPageRetrofitClient.myPageService,
            tokenManager = tokenManager
        )
        MyPageViewModelFactory(repo)
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


        MyPageRetrofitClient.initialize(tokenManager, this)

        // Mock 모드에서 토큰이 없으면 Mock 로그인 정보 설정
        if (!tokenManager.isLoggedIn()) {
            tokenManager.saveMockLoginInfo()
        }

        // ✅ ViewModel에 LocalDataManager 설정
        val localDataManager = com.example.momenty.global.security.LocalDataManager(this)
        myPageViewModel.setLocalDataManager(localDataManager)


        binding = ActivityPetFormManageBinding.inflate(layoutInflater)

        setContentView(binding.root)

        imagePickerHelper = ImagePickerHelper(
            this,
            galleryLauncher,
            cameraLauncher
        )

        observePerformUpdatePetProfile()
        observePerformLoadOnePetProfile()

        val petId = intent.getLongExtra("petId", 0L)
        performLoadOnePetProfile(petId)

        Log.e(TAG, "start ManagyActivity")

        initializeViews()

        observeProfileState()
        observeImageUploadState()
    }

    private fun initializeViews() {
        initListener()
        updateSaveButton()
    }

    private fun initListener() {
        binding.btnPetFormManageBack.setOnClickListener {
            finish()
        }

        // 프로필 이미지 변경
        binding.layoutPetFormManagePhoto.setOnClickListener {
            imagePickerHelper?.showImagePickerDialog()
        }

        // 이름 입력 감지
        binding.etPetFormManageName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        setupClearButton(binding.etPetFormManageName)

        // 성별 선택
        binding.rgPetGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etPetFormManageBirthday.setOnClickListener {
            showDatePicker()
        }

        // 종 선택 (강아지/고양이)
        binding.etPetFormManageType.setOnClickListener {
            showPetTypeDialog()
        }
        binding.etPetFormManageType.isFocusable = false
        binding.etPetFormManageType.isClickable = true

        // 품종 선택
        binding.etPetFormManageTypeDetail.setOnClickListener {
            if (selectedPetType != null) {
                showTypeBreedDialog()
            } else {
                Toast.makeText(
                    this,
                    "먼저 종을 선택해주세요",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.etPetFormManageTypeDetail.isFocusable = false
        binding.etPetFormManageTypeDetail.isClickable = true

        // 소개 입력 감지
        binding.etPetFormManageIntro.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 저장 버튼
        binding.btnPetFormManageSave.setOnClickListener {
            saveProfile()
        }
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
                binding.etPetFormManageBirthday.setText(formattedDate)
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

    /**
     * 이미지 선택 처리
     */
    private fun handleImageSelected(uri: Uri) {
        selectedImageUri = uri

        // 이미지 미리보기
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.ivPetFormManagePetProfileEdit)

        // Presigned URL 방식으로 이미지 업로드
        uploadImageWithPresignedUrl(uri)
    }

    /**
     * Presigned URL 방식으로 이미지 업로드
     */
    private fun uploadImageWithPresignedUrl(uri: Uri) {
        lifecycleScope.launch {
            profileViewModel.uploadImage(this@PetFormManageActivity, uri)
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
                            this@PetFormManageActivity,
                            "이미지 업로드 중...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is ImageUploadState.Success -> {
                        uploadedImageKey = state.imageKey  // 변경: URL → Key
                        Toast.makeText(
                            this@PetFormManageActivity,
                            "이미지 업로드 완료",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "업로드된 이미지 Key: $uploadedImageKey")
                    }
                    is ImageUploadState.Error -> {
                        Toast.makeText(
                            this@PetFormManageActivity,
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
        Log.d(TAG, "프로필 저장 성공")

        Toast.makeText(
            this@PetFormManageActivity,
            "프로필이 저장되었습니다!",
            Toast.LENGTH_SHORT
        ).show()

        // 로컬에도 저장 (캐싱용)
        //saveProfileLocally()

        finish()
    }

    private fun handleProfileError(message: String) {
        Log.e(TAG, "프로필 저장 실패: $message")
        Toast.makeText(
            this@PetFormManageActivity,
            "프로필 저장 실패: $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoading() {
        binding.btnPetFormManageSave.isEnabled = false
        // TODO: 프로그레스바 표시
    }

    private fun hideLoading() {
        binding.btnPetFormManageSave.isEnabled = true
        // TODO: 프로그레스바 숨김
    }


    private fun showPetTypeDialog() {
        val petTypes = arrayOf("강아지", "고양이")

        AlertDialog.Builder(this)
            .setTitle("종 선택")
            .setItems(petTypes) {dialog, which ->
                selectedPetType = petTypes[which]
                binding.etPetFormManageType.setText(selectedPetType)

                // 종 변경 시 품종 초기화
                binding.etPetFormManageTypeDetail.setText("")

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

        AlertDialog.Builder(this)
            .setTitle("품종 선택")
            .setItems(breeds)   { dialog, which ->
                binding.etPetFormManageTypeDetail.setText(breeds[which])
                breedIndex = which
                updateSaveButton()
                dialog.dismiss()
            }
            .setNegativeButton("취소")    {dialog, _ ->
                // 공백 설정
                binding.etPetFormManageTypeDetail.setText("")
                updateSaveButton()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하여 저장 버튼 활성화/비활성화
     */
    private fun updateSaveButton() {
        val hasName = binding.etPetFormManageName.text?.isNotBlank() == true
        val hasGender = binding.rgPetGender.checkedRadioButtonId != -1
        val hasBirth = binding.etPetFormManageBirthday.text?.isNotBlank() == true
        val hasType = binding.etPetFormManageType.text?.isNotBlank() == true
        val allFieldsFilled = hasName && hasGender && hasBirth && hasType

        binding.btnPetFormManageSave.isEnabled = allFieldsFilled

        // 버튼 텍스트 색상 변경
        if (allFieldsFilled) {
            binding.btnPetFormManageSave.setTextColor(
                ContextCompat.getColor(this, R.color.white)
            )
        } else {
            binding.btnPetFormManageSave.setTextColor(
                ContextCompat.getColor(this, R.color.caption_3)
            )
        }
    }

    /**
     * 프로필 정보 저장
     */
    private fun saveProfile() {
        val petName = binding.etPetFormManageName.text.toString()
        val petGender = when (binding.rgPetGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val petBirth = binding.etPetFormManageBirthday.text.toString()
        val petType = if (binding.etPetFormManageType.text.toString() == "강아지") {
            "DOG"
        } else "CAT"
        val petTypeDetail = binding.etPetFormManageTypeDetail.text.toString()
        val petIntro = binding.etPetFormManageIntro.text.toString()

        // 날짜 형식 변환 (YY.MM.DD -> YYYY-MM-DD)
        val petBirthFormatted = formatDateForApi(petBirth)

        performUpdatePetProfile()
        saveProfileLocally(petName, petGender, petBirth, petType, petTypeDetail, petIntro, uploadedImageKey, selectedImageUri)

        finish()
        /*
        // ViewModel을 통해 API 호출 (imageKey 전달)
        profileViewModel.updateProfile(
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
        )*/
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
    private fun saveProfileLocally(
        name: String,
        gender: String,
        birth: String,
        type: String,
        typeDetail: String?,
        intro: String?,
        imageKey: String?,
        imageUri: Uri?) {
        val prefs = getSharedPreferences(
            "momenty_prefs",
            Context.MODE_PRIVATE
        )


        val petId = intent.getLongExtra("petId", 0L)

        if (petId == 0L) {
            prefs.edit().apply {
                putString("pet_name", name)
                putString("pet_gender", gender)
                putString("pet_birth", birth)
                putString("pet_type", type)
                putString("pet_type_detail", typeDetail)
                putString("pet_intro", intro)
                putString("pet_profile_image_key", imageKey)  // 변경: URL → Key
                putString("pet_profile_image_uri", imageUri.toString())
                putBoolean("pet_profile_completed", true)
                apply()
            }
        } else if (petId > 0L) {
            val petData = MyPagePetProfileData(name, gender, birth, type,
                typeDetail, intro, imageKey, imageUri.toString()
            )
            val gson = Gson()
            val petData2Json = gson.toJson(petData)

            prefs.edit().apply {
                putString("pet_info_${petId}", petData2Json)

                apply()
            }
        }
    }

    /*
    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        type: String,
        typeDetail: String,
        intro: String
    ) {
        val prefs = getSharedPreferences(
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

     */

    /*
    private fun getPetProfile() {
        if (loadOnePetProfileDataByApi != null) {
            // TODO: API 데이터 불러오기
            /*
            var tmpData = LoadOnePetProfileData(
                loadOnePetProfileDataByApi?.profileImageUrl,
                loadOnePetProfileDataByApi!!.petName,
                loadOnePetProfileDataByApi!!.gender,
                getFormattedDate(loadOnePetProfileDataByApi?.birth, "yyyy-MM-dd", "yy.MM.dd")!!,
                loadOnePetProfileDataByApi!!.species,
                loadOnePetProfileDataByApi?.breedId,
                loadOnePetProfileDataByApi?.intro
            )*/
            setPetProfile(loadOnePetProfileDataByApi)
        }/* else {
            val spf = getSharedPreferences(
                "momenty_prefs",
                android.content.Context.MODE_PRIVATE
            )

            val petIndex = intent.getLongExtra("petIndex", 0L)

            if (petIndex == 0L) {
                val pet_name = spf.getString("pet_name", "반려동물이름")
                val pet_gender = spf.getString("pet_gender", "male")
                val pet_birth = spf.getString("pet_birth", "00.01.01")
                val pet_type = spf.getString("pet_type", "DOG")
                val pet_type_detail = spf.getString("pet_type_detail", "")
                val pet_intro = spf.getString("pet_intro", "")
                val petProfileImageKey = spf.getString("pet_profile_image_key", null)  // 변경

                setText(pet_name!!, pet_gender!!, pet_birth!!, pet_type!!, pet_type_detail, pet_intro, petProfileImageKey)

            } else if (petIndex > 0L) {
                val gson = Gson()
                val petData = gson.fromJson(spf.getString("pet_info_${petIndex}", null), MyPagePetProfileData::class.java)
                if (petData != null) {
                    setText(petData.name, petData.gender, petData.birth, petData.type, petData.typeDetail, petData.intro, petData.imageKey)
                } else {
                    Toast.makeText(this, "반려동물 데이터 로드 실패", Toast.LENGTH_SHORT).show()
                    return
                }
            } else {
                Toast.makeText(this, "잘못된 인덱스", Toast.LENGTH_SHORT).show()
                return
            }
        }*/

    }*/

    private fun setPetProfile(data: LoadOnePetProfileData?) {
        if (data != null) {
            /*
            val breeds = when(data?.species)  {
                "DOG" -> resources.getStringArray(R.array.dog_type_options)
                "CAT" -> resources.getStringArray(R.array.cat_type_options)
                else -> return
            }*/
            if (!data?.profileImageUrl.isNullOrEmpty()) {
                uploadedImageKey = data?.profileImageUrl

                Glide.with(binding.root.context)
                    .load(uploadedImageKey)
                    .circleCrop()
                    .into(binding.ivPetFormManagePetProfileEdit)
            }

            binding.etPetFormManageName.setText(data?.petName)
            when (data?.gender) {
                "male" -> binding.rgPetGender.check(R.id.rb_gender_male)
                else -> binding.rgPetGender.check(R.id.rb_gender_female)
            }
            binding.etPetFormManageBirthday.setText(data?.birth)
            binding.etPetFormManageType.setText(
                when(data?.species) {
                    "DOG" -> "강아지"
                    "CAT" -> "고양이"
                    else -> ""
                }
            )
            binding.etPetFormManageTypeDetail.setText(data?.breedName)
            /*
            if (tmpBreedId != null) {
                binding.etPetFormManageTypeDetail.setText(breeds[tmpBreedId!!.toInt()])
            }*/
            binding.etPetFormManageIntro.setText(data?.intro)
        }
    }

    /*
    private fun setText(name: String, gender: String, birth: String,
                        type: String, typeDetail: String?, intro: String?, imageKey: String?, uri: String?=null) {

        binding.etPetFormManageName.setText(name)
        when (gender) {
            "male" -> binding.rgPetGender.check(R.id.rb_gender_male)
            else -> binding.rgPetGender.check(R.id.rb_gender_female)
        }
        binding.etPetFormManageBirthday.setText(birth)
        binding.etPetFormManageType.setText(type)
        binding.etPetFormManageTypeDetail.setText(typeDetail)
        binding.etPetFormManageIntro.setText(intro)

        if (!imageKey.isNullOrEmpty()) {
            //val baseUrl = "https://api.momenty.com/"
            //val imageUrl = baseUrl + imageKey
            uploadedImageKey = imageKey

            Glide.with(binding.root.context)
                .load(imageKey)
                .circleCrop()
                .into(binding.ivPetFormManagePetProfileEdit)
        } else if (!uri.isNullOrEmpty()) {
            uploadedImageKey = uri

            Glide.with(binding.root.context)
                .load(Uri.parse(uri))
                .circleCrop()
                .into(binding.ivPetFormManagePetProfileEdit)
        }
    }*/

    private fun performUpdatePetProfile() {
        val petId = intent.getLongExtra("petId", 0L)

        val req = UpdatePetProfileRequest(
            uploadedImageKey,
            binding.etPetFormManageName.text.toString(),
            when (binding.rgPetGender.checkedRadioButtonId) {
                R.id.rb_gender_male -> "male"
                R.id.rb_gender_female -> "female"
                else -> ""
            },
            getFormattedDate(binding.etPetFormManageBirthday.text.toString(),
                "yy.MM.dd", "yyyy-MM-dd")!!,
            binding.etPetFormManageType.text.toString(),

            breedIndex?.toLong(),
            binding.etPetFormManageIntro.text.toString()
        )

        myPageViewModel.updatePetProfile(petId, req)
    }

    private fun observePerformUpdatePetProfile() {
        myPageViewModel.updatePetProfileResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(this, "반려동물 수정 성공!", Toast.LENGTH_SHORT).show()
                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "반려동물 수정 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "반려동물 수정 실패: $message")
                bSuccessApi = false
            }
        }
    }


    private fun performLoadOnePetProfile(petId: Long) {
        myPageViewModel.loadOnePetProfile(petId)
    }

    private fun observePerformLoadOnePetProfile() {
        myPageViewModel.loadOnePetProfileResult.observe(this) { result ->
            result.onSuccess { data ->
                bSuccessApi = true
                Toast.makeText(this, "반려동물 프로필 로드 성공!", Toast.LENGTH_SHORT).show()
                loadOnePetProfileDataByApi = data
                setPetProfile(loadOnePetProfileDataByApi)
                bSuccessApi = false
            }.onFailure { error ->
                val message = error.message ?: "알 수 없는 오류"
                Toast.makeText(this, "반려동물 프로필 로드 실패: $message", Toast.LENGTH_LONG).show()
                Log.d(TAG, "반려동물 프로필 로드 실패: $message")
                bSuccessApi = false
            }
        }
    }

    private fun getFormattedDate(dateString: String?, inputPattern: String, outputPattern: String): String? {
        if (dateString == null) return null

        val inputFormat = DateTimeFormatter.ofPattern(inputPattern)
        val outputFormat = DateTimeFormatter.ofPattern(outputPattern)

        val time = LocalDate.parse(dateString?.trim(), inputFormat)
        return time.format(outputFormat)
    }
}