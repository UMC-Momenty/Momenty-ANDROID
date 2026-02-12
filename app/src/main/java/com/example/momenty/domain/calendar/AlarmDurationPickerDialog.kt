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
import kotlinx.coroutines.*

/**
 *  ANR 해결된 지속 시간 선택 다이얼로그
 * - 백그라운드 뷰 생성
 * - 5분 단위 선택 (60개 → 12개)
 * - 시간은 0-5시간만 표시 (24개 → 6개)
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

    private val dialogScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAlarmDurationPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // 백그라운드에서 뷰 생성
        dialogScope.launch {
            createDurationPickerViews()
        }

        // 초기값 설정
        binding.etDurationPickerHour.setText(String.format("%02d", selectedHour))
        binding.etDurationPickerMin.setText(String.format("%02d", selectedMinute))
    }

    /**
     * 백그라운드에서 뷰 생성
     */
    private suspend fun createDurationPickerViews() = withContext(Dispatchers.Default) {
        // 시간: 0-5시간만 (일반적인 활동 지속 시간)
        val hours = (0..5).map { hour ->
            createItemTextViewData(String.format("%02d", hour)) to hour
        }

        // 분: 5분 단위
        val minutes = (0..59 step 5).map { minute ->
            createItemTextViewData(String.format("%02d", minute)) to minute
        }

        withContext(Dispatchers.Main) {
            if (!isShowing) return@withContext

            // 시간 뷰 추가
            hours.forEach { (textViewData, hour) ->
                val textView = createTextViewFromData(textViewData)
                hourItems.add(textView)
                binding.llHourItems.addView(textView)

                textView.setOnClickListener {
                    selectHour(hour)
                }
            }

            // 분 뷰 추가
            minutes.forEach { (textViewData, minute) ->
                val textView = createTextViewFromData(textViewData)
                minuteItems.add(textView)
                binding.llMinuteItems.addView(textView)

                textView.setOnClickListener {
                    selectMinute(minute)
                }
            }

            // 초기 선택
            selectHour(selectedHour)
            selectMinute(findNearestMinute(selectedMinute))
        }
    }

    private fun createItemTextViewData(text: String): Triple<String, Float, Int> {
        return Triple(text, 14f, 24)
    }

    private fun createTextViewFromData(data: Triple<String, Float, Int>): TextView {
        val (text, textSize, padding) = data
        return TextView(context).apply {
            this.text = text
            this.textSize = textSize
            gravity = android.view.Gravity.CENTER
            setPadding(16, padding, 16, padding)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun findNearestMinute(minute: Int): Int {
        return ((minute + 2) / 5) * 5
    }

    private fun setupListeners() {
        // Debounce TextWatcher
        var hourUpdateJob: Job? = null
        binding.etDurationPickerHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hourUpdateJob?.cancel()
                hourUpdateJob = dialogScope.launch {
                    delay(300)
                    val text = s.toString()
                    if (text.isNotEmpty()) {
                        val hour = text.toIntOrNull()
                        if (hour != null && hour in 0..5) {  // 0-5시간으로 제한
                            selectHour(hour)
                        }
                    }
                }
            }
        })

        var minUpdateJob: Job? = null
        binding.etDurationPickerMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                minUpdateJob?.cancel()
                minUpdateJob = dialogScope.launch {
                    delay(300)
                    val text = s.toString()
                    if (text.isNotEmpty()) {
                        val minute = text.toIntOrNull()
                        if (minute != null && minute in 0..59) {
                            val nearest = findNearestMinute(minute)
                            selectMinute(nearest)
                        }
                    }
                }
            }
        })

        binding.tvBtnCancel.setOnClickListener {
            dismiss()
        }

        binding.tvBtnEnter.setOnClickListener {
            val durationString = String.format("%02d:%02d", selectedHour, selectedMinute)
            onDurationSelected(durationString)
            dismiss()
        }
    }

    private fun selectHour(hour: Int) {
        if (hourItems.isEmpty()) return

        selectedHour = hour
        hourItems.forEach { it.isSelected = false }

        if (hour in hourItems.indices) {
            hourItems[hour].isSelected = true

            binding.svHourPicker.post {
                val selectedView = hourItems[hour]
                val scrollY = selectedView.top - (binding.svHourPicker.height / 2) + (selectedView.height / 2)
                binding.svHourPicker.smoothScrollTo(0, scrollY.coerceAtLeast(0))
            }
        }

        if (binding.etDurationPickerHour.text.toString() != String.format("%02d", hour)) {
            binding.etDurationPickerHour.setText(String.format("%02d", hour))
        }
    }

    private fun selectMinute(minute: Int) {
        if (minuteItems.isEmpty()) return

        val nearest = findNearestMinute(minute)
        selectedMinute = nearest

        minuteItems.forEach { it.isSelected = false }

        val index = nearest / 5
        if (index in minuteItems.indices) {
            minuteItems[index].isSelected = true

            binding.svMinutePicker.post {
                val selectedView = minuteItems[index]
                val scrollY = selectedView.top - (binding.svMinutePicker.height / 2) + (selectedView.height / 2)
                binding.svMinutePicker.smoothScrollTo(0, scrollY.coerceAtLeast(0))
            }
        }

        if (binding.etDurationPickerMin.text.toString() != String.format("%02d", nearest)) {
            binding.etDurationPickerMin.setText(String.format("%02d", nearest))
        }
    }

    override fun dismiss() {
        dialogScope.cancel()
        super.dismiss()
    }
}