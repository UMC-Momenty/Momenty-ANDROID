package com.example.momenty.domain.calendar

import android.app.TimePickerDialog
import android.graphics.Color
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

    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private val calendarViewModel: CalendarViewModel by activityViewModels {
        val helper = DeviceCalendarHelper(requireContext())
        val repo = CalendarRepository(
            apiService = RetrofitClient.calendarApiService,
            deviceCalendarHelper = helper
        )
        CalendarViewModelFactory(repo)
    }

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
    ): View {
        _binding = FragmentAddAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calendar Fragment에서 선택한 날짜 사용

        setupViews()
        setupClickListeners()
        // 초기 상태 설정
        initializeButtonStates()

        showActivityTypeDialog() // Fragment 생성 시 바로 다이얼로그 표시
    }

    private fun setupViews() {
        dayButtons = listOf(
            binding.btnDayMon to Calendar.MONDAY,
            binding.btnDayTue to Calendar.TUESDAY,
            binding.btnDayWed to Calendar.WEDNESDAY,
            binding.btnDayThr to Calendar.THURSDAY,
            binding.btnDayFri to Calendar.FRIDAY,
            binding.btnDaySat to Calendar.SATURDAY,
            binding.btnDaySun to Calendar.SUNDAY
        )
    }

    /**
     * 모든 버튼의 초기 상태 설정
     */
    private fun initializeButtonStates() {
        // 주기 버튼 : 일회성 선택된 상태로 시작
        binding.btnTermOnce.isSelected = true
        binding.btnTermOnce.refreshDrawableState()

        binding.btnTermRepeat.isSelected = false
        binding.btnTermRepeat.refreshDrawableState()

        // 요일 버튼 : 모두 선택 해제
        dayButtons.forEach { (button, _) ->
            button.isSelected = false
            button.refreshDrawableState()
        }

        // 일회성 모드로 시작 (요일 섹션 숨김)
        isRepeatMode = false
        updateRepeatDayVisibility()
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
        // 상태가 실제로 변경될 때만 처리
        if (isRepeatMode == isRepeat) return

        isRepeatMode = isRepeat

        // 버튼 스타일 업데이트
        binding.btnTermOnce.isSelected = !isRepeat
        binding.btnTermOnce.refreshDrawableState()
        binding.btnTermRepeat.isSelected = isRepeat
        binding.btnTermRepeat.refreshDrawableState()

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
            // 모든 요일 버튼 선택 해제
            dayButtons.forEach { (button, _) ->
                button.isSelected = false
                button.refreshDrawableState()
            }
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
        } else {
            selectedRepeatDays.add(dayNum)
            button.isSelected = true
        }
        button.refreshDrawableState()
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
        alarmViewModel.addAlarm(alarm)

        // Alarm을 CalendarEvent로 변환
        val calendarEvent = CalendarEvent(
            id = System.currentTimeMillis().toString(),
            title = alarm.title,
            startTime = alarm.alarmDate,
            endTime = alarm.alarmDate,
            calendarId = "default_calendar", // 기본 캘린더 ID
            color = getColorForActivityType(alarm.activityType),
            petId = null
        )

        calendarViewModel.addEvent(calendarEvent)

        Snackbar.make(binding.root, "알림이 추가되었습니다", Snackbar.LENGTH_SHORT).show()


        // CalendarAlarmFragment로 돌아가기
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun getColorForActivityType(activityType: String): Int {
        return when(activityType) {
            "산책" -> ContextCompat.getColor(requireContext(), R.color.alarm_walk)
            "식사" -> ContextCompat.getColor(requireContext(), R.color.alarm_eat)
            "미용" -> ContextCompat.getColor(requireContext(), R.color.alarm_beauty)
            "건강" -> ContextCompat.getColor(requireContext(), R.color.alarm_health)
            "투약" -> ContextCompat.getColor(requireContext(), R.color.alarm_medician)
            "간식" -> ContextCompat.getColor(requireContext(), R.color.alarm_treat)
            "기타" -> ContextCompat.getColor(requireContext(), R.color.alarm_etc)
            else ->  ContextCompat.getColor(requireContext(), R.color.primary)
        }
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