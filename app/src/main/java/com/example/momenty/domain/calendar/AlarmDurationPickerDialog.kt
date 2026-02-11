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
import com.example.momenty.databinding.DialogAlarmDurationPickerBinding

/**
 * 알림 지속 시간 선택을 위한 커스텀 다이얼로그
 * - 시간: 0~23 선택
 * - 분: 0~59 선택
 */
class AlarmDurationPickerDialog(
    context: Context,
    private val onDurationSelected: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAlarmDurationPickerBinding

    private var selectedHour = 0
    private var selectedMinute = 30

    private val hourItems = mutableListOf<TextView>()
    private val minuteItems = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAlarmDurationPickerBinding.inflate(LayoutInflater.from(context))
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
        // 시간 아이템 생성 (0 ~ 23)
        for (hour in 0..23) {
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
        selectMinute(selectedMinute)

        // EditText 초기값 설정
        binding.etDurationPickerHour.setText(String.format("%02d", selectedHour))
        binding.etDurationPickerMin.setText(String.format("%02d", selectedMinute))
    }

    private fun setupListeners() {
        // 시간 EditText
        binding.etDurationPickerHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val hour = text.toIntOrNull()
                    if (hour != null && hour in 0..23) {
                        selectHour(hour)
                    }
                }
            }
        })

        // 분 EditText
        binding.etDurationPickerMin.addTextChangedListener(object : TextWatcher {
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
            val durationString = String.format("%02d:%02d", selectedHour, selectedMinute)
            onDurationSelected(durationString)
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
        if (hour in 0..23) {
            hourItems[hour].isSelected = true

            // 선택된 아이템으로 스크롤
            binding.svHourPicker.post {
                val selectedView = hourItems[hour]
                val scrollY = selectedView.top - (binding.svHourPicker.height / 2) + (selectedView.height / 2)
                binding.svHourPicker.smoothScrollTo(0, scrollY)
            }
        }

        // EditText 업데이트
        binding.etDurationPickerHour.setText(String.format("%02d", hour))
    }

    /**
     * 분 선택
     */
    private fun selectMinute(minute: Int) {
        selectedMinute = minute

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
        binding.etDurationPickerMin.setText(String.format("%02d", minute))
    }
}