package com.example.momenty.domain.mypage

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.momenty.R
import com.example.momenty.databinding.ActivityPetFormAddBinding
import java.util.Calendar

class PetFormAddActivity: AppCompatActivity() {
    lateinit var binding: ActivityPetFormAddBinding
    private var selectedPetType: String? = null // 강아지 or 고양이


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPetFormAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        binding.btnPetFormAddBack.setOnClickListener {
            finish()
        }

        // 이름 입력 감지
        binding.etPetAddName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 이름 입력 필드 clear 버튼
        binding.etPetAddName.setOnFocusChangeListener { _, hasFocus ->
            // EditText에 drawableEnd로 clear 아이콘이 있다면 클릭 이벤트 추가 가능
        }

        // 성별 선택
        binding.rgPetGender.setOnCheckedChangeListener { _, _ ->
            updateSaveButton()
        }

        // 생년월일 선택
        binding.etPetFormAddBirthday.setOnClickListener {
            showDatePicker()
        }

        // 종 선택 (강아지/고양이)
        binding.etPetFormAddType.setOnClickListener {
            showPetTypeDialog()
        }
        binding.etPetFormAddType.isFocusable = false
        binding.etPetFormAddType.isClickable = true

        // 품종 선택
        binding.etPetFormAddTypeDetail.setOnClickListener {
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
        binding.etPetFormAddTypeDetail.isFocusable = false
        binding.etPetFormAddTypeDetail.isClickable = true

        // 소개 입력 감지
        binding.etPetFormAddIntro.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 저장 버튼
        binding.btnPetFormAddSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun initializeViews() {
        initListener()
        updateSaveButton()
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
                binding.etPetFormAddBirthday.setText(formattedDate)
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

        AlertDialog.Builder(this)
            .setTitle("종 선택")
            .setItems(petTypes) {dialog, which ->
                selectedPetType = petTypes[which]
                binding.etPetFormAddType.setText(selectedPetType)

                // 종 변경 시 품종 초기화
                binding.etPetFormAddTypeDetail.setText("")

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
                binding.etPetFormAddTypeDetail.setText(breeds[which])
                updateSaveButton()
                dialog.dismiss()
            }
            .setNegativeButton("취소")    {dialog, _ ->
                // 공백 설정
                binding.etPetFormAddTypeDetail.setText("")
                updateSaveButton()
                dialog.dismiss()
            }
            .show()
    }

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하여 저장 버튼 활성화/비활성화
     */
    private fun updateSaveButton() {
        val hasName = binding.etPetAddName.text?.isNotBlank() == true
        val hasGender = binding.rgPetGender.checkedRadioButtonId != -1
        val hasBirth = binding.etPetFormAddBirthday.text?.isNotBlank() == true
        val hasType = binding.etPetFormAddType.text?.isNotBlank() == true
        val allFieldsFilled = hasName && hasGender && hasBirth && hasType

        binding.btnPetFormAddSave.isEnabled = allFieldsFilled

        // 버튼 텍스트 색상 변경
        if (allFieldsFilled) {
            binding.btnPetFormAddSave.setTextColor(
                ContextCompat.getColor(this, R.color.white)
            )
        } else {
            binding.btnPetFormAddSave.setTextColor(
                ContextCompat.getColor(this, R.color.caption_3)
            )
        }
    }

    /**
     * 프로필 정보 저장
     */
    private fun saveProfile() {
        val name = binding.etPetAddName.text.toString()
        val gender = when (binding.rgPetGender.checkedRadioButtonId) {
            R.id.rb_gender_male -> "male"
            R.id.rb_gender_female -> "female"
            else -> ""
        }
        val birth = binding.etPetFormAddBirthday.text.toString()
        val type = binding.etPetFormAddType.text.toString()
        val typeDetail = binding.etPetFormAddTypeDetail.text.toString()
        val intro = binding.etPetFormAddIntro.text.toString()


        // SharedPreferences에 저장
        saveToPreferences(name, gender, birth, type, typeDetail, intro)

        // 메인 화면으로 이동
        finish()
    }

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

}