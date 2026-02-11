package com.example.momenty.domain.calendar

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.momenty.R
import com.example.momenty.databinding.DialogAlarmTimePickerBinding

class AlarmTimePickerDialog(
    context: Context,
    private val onTimeSelected: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAlarmTimePickerBinding

    private var selectedAmPm = ""
    private var selectedHour = 0
    private var selectedMin = 0

    private val hourItems = mutableListOf<TextView>()
    private val minuteItems = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAlarmTimePickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // 다이얼로그 배경 투명 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // 오전/오후 라디오 버튼 초기 설정
        binding.rbTimePickerDay.isChecked = true
        updateAmPmEditText()

        // 시간 아이템 생성 (1 ~ 12)
        for (hour in 1..12) {
            val textView = createItemTextView(String.format("%02d", hour))
            hourItems.add(textView)
            binding.llHourItems.addView(textView)

            textView.setOnClickListener {
                selectHour(hour)
            }
        }

        // 분 아이템 생성 (0 ~ 59)
        for (minute in 0..59) {
            val textView = createItemTextView(String.format("%02d", minute))
            minuteItems.add(textView)
            binding.llMinuteItems.addView(textView)

            textView.setOnClickListener {
                selectMinute(minute)
            }
        }

        // 초기 선택 상태 설정
        selectHour(selectedHour)
        selectMinute(selectedMin)

        // EditText 초기값 설정
        binding.etTimePickerHour.setText(String.format("%02d", selectedHour))
        binding.etTimePickerMin.setText(String.format("%02d", selectedMin))
    }

    private fun setupListeners() {
        // 오전/오후 라디오 버튼
        binding.rgTimePickerDayNight.setOnCheckedChangeListener { _, checkedId ->
            selectedAmPm = if (checkedId == R.id.rb_time_picker_day) "오전" else "오후"
            updateAmPmEditText()
        }

        // 시간 EditText
        binding.etTimePickerHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val hour = text.toIntOrNull()
                    if (hour != null && hour in 1..12) {
                        selectHour(hour)
                    }
                }
            }
        })

        // 분 EditText
        binding.etTimePickerMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val minute = text.toIntOrNull()
                    if (minute != null && minute in 0..59) {
                        selectMinute(minute)
                    }
                }
            }
        })

        // 취소 버튼
        binding.tvBtnCancel.setOnClickListener {
            dismiss()
        }

        // 확인 버튼
        binding.tvBtnEnter.setOnClickListener {
            val timeString = String.format("%s %02d:%02d", selectedAmPm, selectedHour, selectedMin)
            onTimeSelected(timeString)
            dismiss()
        }
    }

    /**
     * 스크롤 가능한 아이템 TextView 생성
     */
    private fun createItemTextView(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setPadding(16, 24, 16, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * 시간 선택
     */
    private fun selectHour(hour: Int) {
        selectedHour = hour

        // 모든 시간 아이템 선택 해제
        hourItems.forEach { it.isSelected = false }

        // 선택된 시간 강조
        if (hour in 1..12) {
            hourItems[hour - 1].isSelected = true

            // 선택된 아이템으로 스크롤
            binding.svHourPicker.post {
                val selectedView = hourItems[hour - 1]
                val scrollY = selectedView.top - (binding.svHourPicker.height / 2) + (selectedView.height / 2)
                binding.svHourPicker.smoothScrollTo(0, scrollY)
            }
        }

        // EditText 업데이트
        binding.etTimePickerHour.setText(String.format("%02d", hour))
    }

    /**
     * 분 선택
     */
    private fun selectMinute(minute: Int) {
        selectedMin = minute

        // 모든 분 아이템 선택 해제
        minuteItems.forEach { it.isSelected = false }

        // 선택된 분 강조
        if (minute in 0..59) {
            minuteItems[minute].isSelected = true

            // 선택된 아이템으로 스크롤
            binding.svMinutePicker.post {
                val selectedView = minuteItems[minute]
                val scrollY = selectedView.top - (binding.svMinutePicker.height / 2) + (selectedView.height / 2)
                binding.svMinutePicker.smoothScrollTo(0, scrollY)
            }
        }

        // EditText 업데이트
        binding.etTimePickerMin.setText(String.format("%02d", minute))
    }

    /**
     * 오전/오후 EditText 업데이트
     */
    private fun updateAmPmEditText() {
        binding.etTimePickerDayNight.setText(selectedAmPm)
    }
}