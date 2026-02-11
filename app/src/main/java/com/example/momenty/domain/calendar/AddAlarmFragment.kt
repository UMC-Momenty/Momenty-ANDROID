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
import androidx.lifecycle.lifecycleScope
import com.example.momenty.R
import com.example.momenty.databinding.FragmentAddAlarmBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
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
        binding.btnTermRepeat.isSelected = false

        // 요일 버튼 : 모두 선택 해제
        dayButtons.forEach { (button, _) ->
            button.isSelected = false
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
        // 활동 유형 버튼 - 클릭 시에만 다이얼로그 표시
        binding.btnAlarmActivity.setOnClickListener { showActivityTypeDialog() }
        // 일회성/반복성 토글
        binding.btnTermOnce.setOnClickListener { setRepeatMode(false) }
        binding.btnTermRepeat.setOnClickListener { setRepeatMode(true) }
        // 알림 시간
        binding.etAlarmTime.setOnClickListener {
            if (isAdded && !isDetached) {
                showCustomTimePicker()
            }
        }
        // 지속 시간
        binding.etAlarmContinue.setOnClickListener {
            if (isAdded && !isDetached) {
                showCustomDurationPicker()
            }
        }
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
        if (!isAdded || isDetached || context == null) return

        try{
            ActivityTypeDialog(requireContext()) { activityType ->
                if(isAdded) {
                    selectedActivityType = activityType
                    binding.btnAlarmActivity.text = activityType
                }
            }.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        binding.btnTermRepeat.isSelected = isRepeat

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
            }
        }
    }

    /**
     * 시간 선택 다이얼로그
     */
    private fun showCustomTimePicker() {
        if (!isAdded || context == null) return

        try {
            AlarmTimePickerDialog(requireContext()) { timeString ->
                selectedTime = timeString
                binding.etAlarmTime.setText(timeString)
            }.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCustomDurationPicker() {
        if (!isAdded || context == null) return

        try {
            AlarmDurationPickerDialog(requireContext()) { durationString ->
                binding.etAlarmContinue.setText(durationString)
            }.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        // 알람 생성 - 백그라운드에서 처리
        lifecycleScope.launch {
            try {
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

                alarmViewModel.addAlarm(alarm)

                // Alarm을 CalendarEvent로 변환
                val calendarEvent = CalendarEvent(
                    id = System.currentTimeMillis().toString(),
                    petId = null, // TODO: 선택된 반려동물 ID
                    calendarId = "default_calendar",
                    title = alarm.title,
                    scheduleDate = alarm.alarmDate,
                    alarmTime = alarm.alarmTime,
                    petName = null, // TODO: 선택된 반려동물 이름
                    type = alarm.activityType
                )

                calendarViewModel.addEvent(calendarEvent)

                if (isAdded) {
                    showSnackbar("알림이 추가되었습니다")
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    showSnackbar("알림 추가 중 오류가 발생했습니다")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        if (isAdded && view != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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