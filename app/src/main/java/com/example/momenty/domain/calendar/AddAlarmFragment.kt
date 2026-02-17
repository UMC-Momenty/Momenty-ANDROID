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
import com.example.momenty.domain.calendar.api.request.CreateScheduleRequest
import com.example.momenty.domain.calendar.api.request.DayOfWeek
import com.example.momenty.domain.calendar.api.request.ScheduleCategory
import com.example.momenty.domain.calendar.api.request.ScheduleType
import com.example.momenty.global.security.TokenManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddAlarmFragment : Fragment() {

    private var _binding: FragmentAddAlarmBinding? = null
    private val binding get() = _binding!!

    // TokenManager 주입
    @Inject
    lateinit var tokenManager: TokenManager

    private val alarmViewModel: AlarmViewModel by activityViewModels()

    // ViewModel Factory 제거하고 직접 주입받기
    private val calendarViewModel: CalendarViewModel by activityViewModels {
        val repo = CalendarRepository(
            calendarApiService = RetrofitClient.calendarApiService,
            petApiService = RetrofitClient.petApiService,
            tokenManager = tokenManager
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

        // ✅ RetrofitClient 초기화
        RetrofitClient.initialize(tokenManager)

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

        // 초기 상태 설정
        initializeButtonStates()
    }

    /**
     * 반려동물 필터 설정 - 다중 선택 가능
     */
    private fun setupPetFilter() {
        petFilterAdapter = PetFilterAdapter(
            pets = emptyList(),
            multiSelect = true, // 다중 선택 모드 활성화
            onPetClick = { _ ->
                // 개별 반려동물 선택/해제 시 All 버튼 상태 동기화
                val currentSelected = petFilterAdapter.getSelectedPets()
                val allPets = petFilterAdapter.getAllPets()
                val isAllSelected = allPets.isNotEmpty() && currentSelected.size == allPets.size
                binding.btnPetFilterAll.isSelected = isAllSelected

                if (currentSelected.isEmpty()) {
                    showSnackbar("반려동물을 선택해주세요", Snackbar.LENGTH_SHORT)
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

        // ✅ All 버튼 초기 상태 - 선택 해제
        binding.btnPetFilterAll.isSelected = false

        // ✅ All 버튼 클릭 리스너
        binding.btnPetFilterAll.setOnClickListener {
            val allPets = petFilterAdapter.getAllPets()
            if (allPets.isEmpty()) return@setOnClickListener

            val isCurrentlyAll = binding.btnPetFilterAll.isSelected

            if (isCurrentlyAll) {
                // All이 선택된 상태 → 전체 해제
                petFilterAdapter.clearAllSelections()
                binding.btnPetFilterAll.isSelected = false
            } else {
                // All이 선택 안 된 상태 → 전체 선택
                petFilterAdapter.selectAllPets()
                binding.btnPetFilterAll.isSelected = true
                showSnackbar("${allPets.size}마리 선택됨", Snackbar.LENGTH_SHORT)
            }
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
                showTimePickerDialog()
            }
        }

        // 요일 선택 버튼
        dayButtons.forEach { (button, dayNum) ->
            button.setOnClickListener {
                toggleDaySelection(button, dayNum)
            }
        }

        // 저장 버튼
        binding.btnAlarmSave.setOnClickListener {
            saveAlarm()
        }
    }

    /**
     * 일회성/반복성 모드 전환
     */
    private fun setRepeatMode(isRepeat: Boolean) {
        isRepeatMode = isRepeat

        // 버튼 상태 업데이트
        binding.btnTermOnce.isSelected = !isRepeat
        binding.btnTermRepeat.isSelected = isRepeat

        // 요일 섹션 표시 여부
        if (isRepeat) {
            binding.tvAlramDateLabel.visibility = View.VISIBLE
            binding.llDateBtns.visibility = View.VISIBLE
        } else {
            binding.tvAlramDateLabel.visibility = View.GONE
            binding.llDateBtns.visibility = View.GONE
            // 선택된 요일 초기화
            selectedRepeatDays.clear()
            dayButtons.forEach { (button, _) ->
                button.isSelected = false
            }
        }
    }

    /**
     * 요일 선택/해제 토글
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
     * 활동 유형 선택 다이얼로그
     */
    private fun showActivityTypeDialog() {
        val activityTypes = arrayOf("산책", "식사", "건강", "미용", "투약", "간식", "기타")

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("활동 유형 선택")
        builder.setItems(activityTypes) { _, which ->
            selectedActivityType = activityTypes[which]
            binding.etAlarmName.setText(selectedActivityType)
        }
        builder.show()
    }

    /**
     * 시간 선택 다이얼로그
     */
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                binding.etAlarmTime.setText(selectedTime)
            },
            currentHour,
            currentMinute,
            true // 24시간 형식
        )
        timePickerDialog.show()
    }

    /**
     * ✅ 알람 저장 - LocalDataManager와 ViewModel 모두 업데이트
     */
    private fun saveAlarm() {
        // 유효성 검사 1: 반려동물 선택 필수
        val selectedPets = petFilterAdapter.getSelectedPets()
        if (selectedPets.isEmpty()) {
            showSnackbar("반려동물을 최소 1개 선택해주세요", Snackbar.LENGTH_SHORT)
            return
        }

        // 유효성 검사 2: 활동 유형 선택 필수
        if (selectedActivityType == null) {
            showSnackbar("활동 유형을 선택해주세요", Snackbar.LENGTH_SHORT)
            return
        }

        // 유효성 검사 3: 알림 시간 선택 필수
        if (selectedTime == null) {
            showSnackbar("알림 시간을 선택해주세요", Snackbar.LENGTH_SHORT)
            return
        }

        // 유효성 검사 4: 반복성일 경우 요일 선택 필수
        if (isRepeatMode && selectedRepeatDays.isEmpty()) {
            showSnackbar("알림 요일을 선택해주세요", Snackbar.LENGTH_SHORT)
            return
        }

        // ✅ LocalDataManager 초기화
        val localDataManager = com.example.momenty.global.security.LocalDataManager(requireContext())

        // ✅ 선택한 각 반려동물별로 알람 생성 및 저장
        lifecycleScope.launch {
            try {
                var successCount = 0

                selectedPets.forEach { pet ->
                    // CreateScheduleRequest 생성
                    val request = createScheduleRequest()

                    // 1. 백엔드 API 호출 (Mock)
                    val apiSuccess = calendarViewModel.createSchedule(pet.petId, request)

                    if (apiSuccess) {
                        // 2. LocalDataManager에 저장
                        val schedule = com.example.momenty.global.mock.LocalSchedule(
                            scheduleId = System.currentTimeMillis() + pet.petId, // 고유 ID 생성
                            petId = pet.petId,
                            title = request.title,
                            category = request.category.name,
                            date = request.date,
                            repeatDays = request.repeatDays?.map { it.name },
                            time = request.time,
                            durationMinutes = request.durationMinutes,
                            memo = request.memo,
                            isAlarmEnabled = request.isAlarmEnabled,
                            isOneTime = request.type == ScheduleType.ONE_TIME
                        )

                        localDataManager.addSchedule(schedule)

                        android.util.Log.d("AddAlarm", "Schedule saved: ${schedule.title} for pet ${pet.petId}")
                        successCount++
                    }
                }

                if (isAdded) {
                    if (successCount > 0) {
                        showSnackbar(
                            "${successCount}개의 알림이 추가되었습니다",
                            Snackbar.LENGTH_SHORT
                        )

                        // ✅ AlarmViewModel 새로고침 (알람 목록 화면용)
                        alarmViewModel.loadAlarms()

                        // ✅ CalendarViewModel 새로고침 (달력 화면에 반영)
                        calendarViewModel.loadSchedules()

                        // 약간의 지연 후 뒤로 가기 (데이터 로드 완료 대기)
                        kotlinx.coroutines.delay(300)
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        showSnackbar("알림 추가에 실패했습니다", Snackbar.LENGTH_SHORT)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("AddAlarm", "Error saving alarm", e)
                if (isAdded) {
                    showSnackbar("알림 추가 중 오류가 발생했습니다", Snackbar.LENGTH_SHORT)
                }
            }
        }
    }

    /**
     * ✅ CreateScheduleRequest 생성
     */
    private fun createScheduleRequest(): CreateScheduleRequest {
        // 카테고리 매핑
        val category = mapActivityTypeToCategory(selectedActivityType!!)

        // 날짜 형식 변환 ("YYYY-MM-DD")
        val dateString = if (!isRepeatMode) {
            formatDateToString(selectedDate)
        } else {
            null
        }

        // 요일 변환 (Int → DayOfWeek enum)
        val repeatDaysEnum = if (isRepeatMode) {
            selectedRepeatDays.map { dayNum ->
                mapIntToDayOfWeek(dayNum)
            }
        } else {
            null
        }

        // 지속시간 파싱 (String → Int)
        val durationText = binding.etAlarmContinue.text.toString().trim()
        val durationMinutes = parseDurationToMinutes(durationText)

        // 메모
        val memo = binding.etAlarmNote.text.toString().trim().takeIf { it.isNotEmpty() }

        return CreateScheduleRequest(
            title = selectedActivityType!!,
            category = category,
            memo = memo,
            time = selectedTime!!,  // "HH:mm"
            type = if (isRepeatMode) ScheduleType.REPEAT else ScheduleType.ONE_TIME,
            date = dateString,
            repeatDays = repeatDaysEnum,
            durationMinutes = durationMinutes,
            isAlarmEnabled = true
        )
    }

    /**
     * 활동 유형을 ScheduleCategory로 매핑
     */
    private fun mapActivityTypeToCategory(activityType: String): ScheduleCategory {
        return when (activityType) {
            "산책" -> ScheduleCategory.WALK
            "식사" -> ScheduleCategory.MEAL
            "건강" -> ScheduleCategory.HEALTH
            "미용" -> ScheduleCategory.BEAUTY
            "투약" -> ScheduleCategory.MEDICINE
            "간식" -> ScheduleCategory.SNACK
            else -> ScheduleCategory.ETC
        }
    }

    /**
     * Int 요일을 DayOfWeek enum으로 변환
     * 1=월요일, 2=화요일, ..., 7=일요일
     */
    private fun mapIntToDayOfWeek(dayNum: Int): DayOfWeek {
        return when (dayNum) {
            1 -> DayOfWeek.MONDAY
            2 -> DayOfWeek.TUESDAY
            3 -> DayOfWeek.WEDNESDAY
            4 -> DayOfWeek.THURSDAY
            5 -> DayOfWeek.FRIDAY
            6 -> DayOfWeek.SATURDAY
            7 -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
    }

    /**
     * Date를 "YYYY-MM-DD" 형식으로 변환
     */
    private fun formatDateToString(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

    /**
     * 지속시간 텍스트를 분(Int)으로 변환
     * 예: "1시간" → 60, "30분" → 30, "1시간 30분" → 90
     */
    private fun parseDurationToMinutes(durationText: String): Int? {
        if (durationText.isEmpty()) return null

        return try {
            var totalMinutes = 0

            // "1시간" 파싱
            val hourRegex = """(\d+)시간""".toRegex()
            hourRegex.find(durationText)?.let { match ->
                val hours = match.groupValues[1].toInt()
                totalMinutes += hours * 60
            }

            // "30분" 파싱
            val minuteRegex = """(\d+)분""".toRegex()
            minuteRegex.find(durationText)?.let { match ->
                val minutes = match.groupValues[1].toInt()
                totalMinutes += minutes
            }

            if (totalMinutes > 0) totalMinutes else null
        } catch (e: Exception) {
            null
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