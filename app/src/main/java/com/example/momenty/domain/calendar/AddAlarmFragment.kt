package com.example.momenty.domain.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.databinding.FragmentAddAlarmBinding
import com.example.momenty.global.security.TokenManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint  // ← Hilt 어노테이션 추가
class AddAlarmFragment : Fragment() {

    private var _binding: FragmentAddAlarmBinding? = null
    private val binding get() = _binding!!

    // TokenManager 주입
    @Inject
    lateinit var tokenManager: TokenManager

    private val alarmViewModel: AlarmViewModel by activityViewModels()

    // ViewModel Factory 제거하고 직접 주입받기
    private val calendarViewModel: CalendarViewModel by activityViewModels {
        val helper = DeviceCalendarHelper(requireContext())
        val repo = CalendarRepository(
            apiService = RetrofitClient.calendarApiService,
            petApiService = RetrofitClient.petApiService,  // ← 추가
            deviceCalendarHelper = helper,
            tokenManager = tokenManager  // ← 추가
        )
        CalendarViewModelFactory(repo)
    }

    private lateinit var petFilterAdapter: PetFilterAdapter
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

        // RetrofitClient 초기화
        RetrofitClient.initialize(tokenManager, requireContext())

        // Calendar Fragment에서 선택한 날짜 사용
        calendarViewModel.uiState.value.selectedDate?.let { calendarDay ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, calendarDay.year)
                set(Calendar.MONTH, calendarDay.month)
                set(Calendar.DAY_OF_MONTH, calendarDay.day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDate = calendar.time
        }

        setupViews()
        setupPetFilter()
        setupClickListeners()
        observeViewModel()

        // 초기 상태 설정 - onViewCreated에서 명시적으로 설정
        initializeButtonStates()
    }

    /**
     * 반려동물 필터 설정 - 다중 선택 가능
     */
    private fun setupPetFilter() {
        petFilterAdapter = PetFilterAdapter(
            pets = emptyList(),
            multiSelect = true, // 다중 선택 모드 활성화
            onPetClick = { pet ->
                // Adapter 내부에서 선택/해제 처리됨
                // getSelectedPets()로 현재 선택된 펫 목록 가져오기
                val currentSelected = petFilterAdapter.getSelectedPets()

                if (currentSelected.isEmpty()) {
                    showSnackbar("반려동물을 선택해주세요", Snackbar.LENGTH_SHORT)
                } else {
                    val names = currentSelected.joinToString(", ") { it.name }
                    showSnackbar("$names 선택됨", Snackbar.LENGTH_SHORT)
                }
            }
        )

        binding.rvAlarmPetList.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = petFilterAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    /**
     * ViewModel 관찰 - StateFlow 사용
     */
    private fun observeViewModel() {
        // 즉시 현재 상태로 UI 업데이트
        val currentState = calendarViewModel.uiState.value
        android.util.Log.d("AddAlarmFragment", "Initial pets: ${currentState.pets.size}")
        if (currentState.pets.isNotEmpty()) {
            petFilterAdapter.updatePets(currentState.pets)
            petFilterAdapter.selectPet(currentState.selectedPet)
        }

        // 이후 변경사항 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                calendarViewModel.uiState.collect { state ->
                    android.util.Log.d("AddAlarmFragment", "Received pets: ${state.pets.size}")
                    // 반려동물 목록 업데이트
                    if (state.pets.isNotEmpty()) {
                        petFilterAdapter.updatePets(state.pets)
                        petFilterAdapter.selectPet(state.selectedPet)
                    }

                    // 선택된 날짜가 변경되면 업데이트
                    state.selectedDate?.let { calendarDay ->
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.YEAR, calendarDay.year)
                            set(Calendar.MONTH, calendarDay.month)
                            set(Calendar.DAY_OF_MONTH, calendarDay.day)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        selectedDate = calendar.time
                    }

                    // 에러 처리
                    state.error?.let { error ->
                        showSnackbar(error, Snackbar.LENGTH_SHORT)
                        calendarViewModel.clearError()
                    }
                }
            }
        }
    }

    private fun setupViews() {
        dayButtons = listOf(
            binding.btnDayMon to 1, // 월요일 = 1
            binding.btnDayTue to 2, // 화요일 = 2
            binding.btnDayWed to 3, // 수요일 = 3
            binding.btnDayThr to 4, // 목요일 = 4
            binding.btnDayFri to 5, // 금요일 = 5
            binding.btnDaySat to 6, // 토요일 = 6
            binding.btnDaySun to 7  // 일요일 = 7
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
        binding.tvAlramDateLabel.visibility = View.GONE
        binding.llDateBtns.visibility = View.GONE
    }

    private fun setupClickListeners() {
        // 뒤로가기
        binding.ivAlarmBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 일정 이름 EditText - 클릭 시 활동 유형 다이얼로그 표시
        binding.etAlarmName.setOnClickListener {
            if (isAdded && !isDetached) {
                showActivityTypeDialog()
            }
        }

        // 일회성/반복성 토글
        binding.btnTermOnce.setOnClickListener {
            setRepeatMode(false)
        }
        binding.btnTermRepeat.setOnClickListener {
            setRepeatMode(true)
        }

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
        binding.btnAlarmSave.setOnClickListener {
            saveAlarm()
        }
    }

    /**
     * 활동 유형 선택 다이얼로그
     */
    private fun showActivityTypeDialog() {
        if (!isAdded || isDetached || context == null) return

        try {
            ActivityTypeDialog(requireContext()) { activityType ->
                if (isAdded) {
                    selectedActivityType = activityType
                    binding.etAlarmName.setText(activityType)
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
        // 유효성 검사 1: 반려동물 선택 필수
        val selectedPets = petFilterAdapter.getSelectedPets()
        if (selectedPets.isEmpty()) {
            Snackbar.make(binding.root, "반려동물을 최소 1개 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 유효성 검사 2: 활동 유형 선택 필수
        if (selectedActivityType == null) {
            Snackbar.make(binding.root, "활동 유형을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 유효성 검사 3: 알림 시간 선택 필수
        if (selectedTime == null) {
            Snackbar.make(binding.root, "알림 시간을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 유효성 검사 4: 반복성일 경우 요일 선택 필수
        if (isRepeatMode && selectedRepeatDays.isEmpty()) {
            Snackbar.make(binding.root, "알림 요일을 선택해주세요", Snackbar.LENGTH_SHORT).show()
            return
        }

        // 선택한 각 반려동물별로 알람 생성
        lifecycleScope.launch {
            try {
                selectedPets.forEach { pet ->
                    val alarm = Alarm(
                        activityType = selectedActivityType!!,
                        petId = pet.id,
                        title = selectedActivityType!!,
                        isRepeat = isRepeatMode,
                        repeatDays = if (isRepeatMode) selectedRepeatDays.toList() else null,
                        alarmDate = selectedDate,
                        alarmTime = selectedTime!!,
                        duration = binding.etAlarmContinue.text.toString().trim().takeIf { it.isNotEmpty() },
                        note = binding.etAlarmNote.text.toString().trim().takeIf { it.isNotEmpty() }
                    )

                    alarmViewModel.addAlarm(alarm)

                    val eventCalendar = Calendar.getInstance().apply {
                        time = selectedDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Alarm을 CalendarEvent로 변환
                    val calendarEvent = CalendarEvent(
                        id = System.currentTimeMillis().toString() + "_" + pet.id,
                        petId = pet.id,
                        calendarId = "default_calendar",
                        title = alarm.title,
                        scheduleDate = eventCalendar.time,
                        alarmTime = alarm.alarmTime,
                        petName = pet.name,
                        type = alarm.activityType
                    )

                    calendarViewModel.addEvent(calendarEvent)
                }

                if (isAdded) {
                    val petNames = selectedPets.joinToString(", ") { it.name }
                    showSnackbar("$petNames 의 알림이 추가되었습니다", Snackbar.LENGTH_SHORT)
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) {
                    showSnackbar("알림 추가 중 오류가 발생했습니다", Snackbar.LENGTH_SHORT)
                }
            }
        }
    }

    private fun showSnackbar(message: String, duration: Int) {
        if (isAdded && view != null) {
            Snackbar.make(binding.root, message, duration).show()
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