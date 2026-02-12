package com.example.momenty.domain.mypage

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.momenty.R
import com.example.momenty.databinding.ActivityUserProfileBinding
import java.util.Calendar

class UserProfileActivity: AppCompatActivity() {
    lateinit var binding: ActivityUserProfileBinding
    private var isUpdating = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeViews()
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
        binding.etUserProfileName.setOnFocusChangeListener { _, hasFocus ->
            // EditText에 drawableEnd로 clear 아이콘이 있다면 클릭 이벤트 추가 가능
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

        // SharedPreferences에 저장
        saveToPreferences(name, gender, birth, alarmTime)


        finish()
    }

    private fun getUserProfile() {
        val spf = getSharedPreferences(
            "momenty_prefs",
            android.content.Context.MODE_PRIVATE
        )
        val user_name = spf.getString("user_name", "사용자")
        val user_gender = spf.getString("user_gender", "male")
        val user_birth = spf.getString("user_birth", "00.01.01")
        val alarm_time: String ?= spf.getString("alarm_time", null)

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

    }

    private fun saveToPreferences(
        name: String,
        gender: String,
        birth: String,
        alarmTime: String?
    ) {
        val prefs = getSharedPreferences(
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

    private fun getSpinnerIndex(sp: Spinner, item: String): Int{
        for (i in 0..sp.count-1) {
            if (sp.getItemAtPosition(i).toString() == item) {
                return i
            }
        }
        return 0
    }
}