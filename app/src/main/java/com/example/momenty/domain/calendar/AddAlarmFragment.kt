package com.example.momenty.domain.calendar

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.momenty.R
import com.example.momenty.databinding.FragmentAddAlarmBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import java.util.Date

class AddAlarmFragment : Fragment() {

    private var _binding: FragmentAddAlarmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlarmViewModel by activityViewModels()

    private var selectedActivityType: String? = null
    private var isRepeatMode = false
    private var selectedDate: Date = Date()
    private var selectedTime: String? = null
    private var selectedRepeatDays = mutableListOf<Int>()

    // 요일 버튼
    private lateinit var dayButtons: List<Pair<Button, Int>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calendar Fragment에서 선택한 날짜 사용

        setupViews()
        setupClickListeners()
        showActivityTypeDialog() // Fragment 생성 시 바로 다이얼로그 표시
        updateRepeatDayVisibility()
    }

    private fun setupViews() {
        dayButtons = listOf(
            binding.btnDayMon to 1,
            binding.btnDayTue to 2,
            binding.btnDayWed to 3,
            binding.btnDayThr to 4,
            binding.btnDayFri to 5,
            binding.btnDaySat to 6,
            binding.btnDaySun to 7
        )
    }

    private fun setupClickListeners() {
        // 뒤로가기
        binding.ivAlarmBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // 활동 유형 버튼 - 다이얼로그 다시 띄우기
        binding.btnAlarmActivity.setOnClickListener { showActivityTypeDialog() }
        // 일회성/반복성 토글
        binding.btnTermOnce.setOnClickListener { setRepeatMode(false) }
        binding.btnTermRepeat.setOnClickListener { setRepeatMode(true) }
        // 알림 시간
        binding.etAlarmTime.setOnClickListener { showTimePicker() }
        // 요일 버튼
        dayButtons.forEach { (button, dayNum) ->
            button.setOnClickListener {
                toggleDaySelection(button, dayNum)
            }
        }
        // 저장
        binding.btnAlarmSave.setOnClickListener { saveAlarm() }
    }

    /**
     * 활동 유형 선택 다이얼로그
     */
    private fun showActivityTypeDialog() {
        ActivityTypeDialog(requireContext()) { activityType ->
            selectedActivityType = activityType
            binding.btnAlarmActivity.text = activityType
        }.show()
    }

    /**
     * 반복 모드 설정 (일회성/반복성)
     */
    private fun setRepeatMode(isRepeat: Boolean) {
        isRepeatMode = isRepeat

        // 버튼 스타일 업데이트
        if (isRepeat) {
            binding.btnTermOnce.isSelected = false
            binding.btnTermRepeat.isSelected = true
            binding.btnTermOnce.setBackgroundResource(R.drawable.bg_button_alarm_toggle)
            binding.btnTermRepeat.setBackgroundResource(R.drawable.bg_button_alarm_toggle)
        } else {
            binding.btnTermOnce.isSelected = true
            binding.btnTermRepeat.isSelected = false
            binding.btnTermOnce.setBackgroundResource(R.drawable.bg_button_alarm_toggle)
            binding.btnTermRepeat.setBackgroundResource(R.drawable.bg_button_alarm_toggle)
        }

        updateRepeatDayVisibility()
    }

    /**
     * 반복 요일 섹션 가시성 업데이트
     */
    private fun updateRepeatDayVisibility() {
        if (isRepeatMode) {
            // 반복성: 알림 요일 섹션 전체 표시
            binding.tvAlramDateLabel.visibility = View.VISIBLE
            binding.tvAlramDateLabel.text = "알림 요일"
            binding.llDateBtns.visibility = View.VISIBLE
        } else {
            // 일회성: 알림 요일 섹션 전체 숨김
            binding.tvAlramDateLabel.visibility = View.GONE
            binding.llDateBtns.visibility = View.GONE
            selectedRepeatDays.clear()
        }
    }

    /**
     * 시간 선택 다이얼로그
     */
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay < 12) "오전" else "오후"
                val hour = if (hourOfDay == 0) 12
                else if (hourOfDay > 12) hourOfDay - 12
                else hourOfDay
                selectedTime = String.format("%s %02d:%02d", amPm, hour, minute)
                binding.etAlarmTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    /**
     * 요일 선택 토글
     */
    private fun toggleDaySelection(button: Button, dayNum: Int) {
        if (selectedRepeatDays.contains(dayNum)) {
            selectedRepeatDays.remove(dayNum)
            button.isSelected = false
            button.setBackgroundResource(R.drawable.bg_alarm_date_selected)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.body_1))
        } else {
            selectedRepeatDays.add(dayNum)
            button.isSelected = true
            button.setBackgroundResource(R.drawable.bg_alarm_date_selected)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    /**
     * 알람 저장
     */
    private fun saveAlarm() {
        // 유효성 검사
        if (selectedActivityType == null) {
            Snackbar.make(binding.root, "활동 유형을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        val title = binding.etAlarmName.text.toString().trim()
        if (title.isEmpty()) {
            Snackbar.make(binding.root, "알림 이름을 입력해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (selectedTime == null) {
            Snackbar.make(binding.root, "알림 시간을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (isRepeatMode && selectedRepeatDays.isEmpty()) {
            Snackbar.make(binding.root, "알림 요일을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 알람 생성
        val alarm = Alarm(
            activityType = selectedActivityType!!,
            title = title,
            isRepeat = isRepeatMode,
            repeatDays = if (isRepeatMode) selectedRepeatDays.toList() else null,
            alarmDate = selectedDate,
            alarmTime = selectedTime!!,
            duration = binding.etAlarmContinue.text.toString().trim().takeIf { it.isNotEmpty() },
            note = binding.etAlarmNote.text.toString().trim().takeIf { it.isNotEmpty() }
        )

        // ViewModel에 추가
        viewModel.addAlarm(alarm)

        // CalendarAlarmFragment로 돌아가기
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddAlarmFragment()

        @JvmStatic
        fun newInstance(selectedDate: Long): AddAlarmFragment {
            return AddAlarmFragment().apply {
                arguments = Bundle().apply {
                    putLong("selectedDate", selectedDate)
                }
            }
        }
    }
}