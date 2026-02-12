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
import kotlinx.coroutines.*

/**
 *   ANR 해결된 시간 선택 다이얼로그
 * - 뷰 생성을 백그라운드 스레드에서 처리
 * - 5분 단위 선택으로 뷰 개수 감소 (60개 → 12개)
 * - Debounce 패턴으로 불필요한 업데이트 방지
 */
class AlarmTimePickerDialog(
    context: Context,
    private val onTimeSelected: (String) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAlarmTimePickerBinding

    private var selectedAmPm = "오전"
    private var selectedHour = 9
    private var selectedMin = 0

    private val hourItems = mutableListOf<TextView>()
    private val minuteItems = mutableListOf<TextView>()

    // 코루틴 스코프
    private val dialogScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAlarmTimePickerBinding.inflate(LayoutInflater.from(context))
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
        // 오전/오후 초기 설정
        binding.rbTimePickerDay.isChecked = true
        updateAmPmEditText()

        // 백그라운드에서 뷰 생성
        dialogScope.launch {
            createTimePickerViews()
        }

        // 초기값 설정
        binding.etTimePickerHour.setText(String.format("%02d", selectedHour))
        binding.etTimePickerMin.setText(String.format("%02d", selectedMin))
    }

    /**
     * 백그라운드에서 뷰 생성 후 메인 스레드에서 추가
     */
    private suspend fun createTimePickerViews() = withContext(Dispatchers.Default) {
        // 1. 시간 아이템 생성 (1-12)
        val hours = (1..12).map { hour ->
            createItemTextViewData(String.format("%02d", hour)) to hour
        }

        // 2. 분 아이템 생성 (5분 단위로 12개만)
        val minutes = (0..59 step 5).map { minute ->
            createItemTextViewData(String.format("%02d", minute)) to minute
        }

        // 메인 스레드에서 뷰 추가
        withContext(Dispatchers.Main) {
            if (!isShowing) return@withContext  // 다이얼로그가 닫힌 경우 중단

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

            // 초기 선택 상태 설정
            selectHour(selectedHour)
            selectMinute(findNearestMinute(selectedMin))
        }
    }

    /**
     * 백그라운드에서 생성할 뷰 데이터
     */
    private fun createItemTextViewData(text: String): Triple<String, Float, Int> {
        return Triple(text, 14f, 24)  // text, textSize, padding
    }

    /**
     * 메인 스레드에서 실제 뷰 생성
     */
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

    /**
     * 가장 가까운 5분 단위 찾기
     */
    private fun findNearestMinute(minute: Int): Int {
        return ((minute + 2) / 5) * 5  // 반올림
    }

    private fun setupListeners() {
        // 오전/오후 라디오 버튼
        binding.rgTimePickerDayNight.setOnCheckedChangeListener { _, checkedId ->
            selectedAmPm = if (checkedId == R.id.rb_time_picker_day) "오전" else "오후"
            updateAmPmEditText()
        }

        // Debounce TextWatcher
        var hourUpdateJob: Job? = null
        binding.etTimePickerHour.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                hourUpdateJob?.cancel()
                hourUpdateJob = dialogScope.launch {
                    delay(300)  // 300ms 디바운스
                    val text = s.toString()
                    if (text.isNotEmpty()) {
                        val hour = text.toIntOrNull()
                        if (hour != null && hour in 1..12) {
                            selectHour(hour)
                        }
                    }
                }
            }
        })

        var minUpdateJob: Job? = null
        binding.etTimePickerMin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                minUpdateJob?.cancel()
                minUpdateJob = dialogScope.launch {
                    delay(300)  // 300ms 디바운스
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
     * 시간 선택
     */
    private fun selectHour(hour: Int) {
        if (hourItems.isEmpty()) return

        selectedHour = hour

        // 모든 시간 아이템 선택 해제
        hourItems.forEach { it.isSelected = false }

        // 선택된 시간 강조
        val index = hour - 1
        if (index in hourItems.indices) {
            hourItems[index].isSelected = true

            // 스크롤 (부드럽게)
            binding.svHourPicker.post {
                val selectedView = hourItems[index]
                val scrollY = selectedView.top - (binding.svHourPicker.height / 2) + (selectedView.height / 2)
                binding.svHourPicker.smoothScrollTo(0, scrollY.coerceAtLeast(0))
            }
        }

        // EditText 업데이트 (리스너 트리거 방지)
        if (binding.etTimePickerHour.text.toString() != String.format("%02d", hour)) {
            binding.etTimePickerHour.setText(String.format("%02d", hour))
        }
    }

    /**
     * 분 선택 (5분 단위)
     */
    private fun selectMinute(minute: Int) {
        if (minuteItems.isEmpty()) return

        val nearest = findNearestMinute(minute)
        selectedMin = nearest

        // 모든 분 아이템 선택 해제
        minuteItems.forEach { it.isSelected = false }

        // 선택된 분 강조 (5분 단위 인덱스)
        val index = nearest / 5
        if (index in minuteItems.indices) {
            minuteItems[index].isSelected = true

            // 스크롤
            binding.svMinutePicker.post {
                val selectedView = minuteItems[index]
                val scrollY = selectedView.top - (binding.svMinutePicker.height / 2) + (selectedView.height / 2)
                binding.svMinutePicker.smoothScrollTo(0, scrollY.coerceAtLeast(0))
            }
        }

        // EditText 업데이트
        if (binding.etTimePickerMin.text.toString() != String.format("%02d", nearest)) {
            binding.etTimePickerMin.setText(String.format("%02d", nearest))
        }
    }

    /**
     * 오전/오후 EditText 업데이트
     */
    private fun updateAmPmEditText() {
        binding.etTimePickerDayNight.setText(selectedAmPm)
    }

    override fun dismiss() {
        dialogScope.cancel()  // 코루틴 정리
        super.dismiss()
    }
}