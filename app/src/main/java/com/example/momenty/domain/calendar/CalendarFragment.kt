package com.example.momenty.domain.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var petFilterAdapter: PetFilterAdapter

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var scheduleAdapter: ScheduleBottomSheetAdapter? = null

    // 추가: ItemDecoration 중복 방지 플래그
    private var isItemDecorationAdded = false

    // 수정: activityViewModels로 변경 (AddAlarmFragment와 공유)
    private val viewModel: CalendarViewModel by activityViewModels {
        val helper = DeviceCalendarHelper(requireContext())
        val repo = CalendarRepository(
            apiService = RetrofitClient.calendarApiService,
            deviceCalendarHelper = helper
        )
        CalendarViewModelFactory(repo)
    }

    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false

        if (readGranted && writeGranted) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(200)
                if (isAdded) loadDataSequentially()
            }
        } else {
            showSnackbar("캘린더 권한이 필요합니다.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI 먼저 초기화
        setupWeekdayHeader()
        setupCalendarRecyclerView()
        setupPetFilter()
        setupClickListeners()

        // StateFlow 관찰
        observeViewModel()

        // ViewModel 초기화 및 권한 확인
        viewLifecycleOwner.lifecycleScope.launch {
            // ViewModel 초기화 - 즉시 실행하여 달력 표시
            viewModel.initialize()

            // 초기 달력 표시를 위한 강제 업데이트
            delay(50)
            if (isAdded) {
                calendarAdapter.updateDays(viewModel.uiState.value.calendarDays)
                binding.tvYearMonthLabel.text = viewModel.getYearMonthText()
            }

            // 권한 확인 및 데이터 로드
            delay(50)
            if (isAdded) {
                checkCalendarPermission()
            }
        }
    }

    /**
     * 데이터를 순차적으로 로드
     */
    private fun loadDataSequentially() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(100)
                if (!isAdded) return@launch

                viewModel.loadAvailableCalendars()
                delay(50)
                if (!isAdded) return@launch

                viewModel.loadPets()

            } catch (e: Exception) {
                showSnackbar("데이터 로드 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }

    /**
     * 반려동물 필터 설정
     */
    private fun setupPetFilter() {
        petFilterAdapter = PetFilterAdapter(
            pets = emptyList(),
            multiSelect = false, // 단일 선택 모드
            onPetClick = { pet ->
                viewModel.selectPetFilter(pet)

                val message = if (pet == null) {
                    "전체 일정을 표시합니다"
                } else {
                    "${pet.name}의 일정을 표시합니다"
                }
                showSnackbar(message, Snackbar.LENGTH_SHORT)

                // 반려동물 필터 클릭 시 바텀시트 표시
                showScheduleBottomSheet()
            }
        )

        binding.rvPetFilter.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = petFilterAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            recycledViewPool.setMaxRecycledViews(0, 10)
        }
    }

    /**
     * 요일 헤더 설정
     */
    private fun setupWeekdayHeader() {
        val weekdays = resources.getStringArray(R.array.calendar_date)
        binding.llCalendarDate.removeAllViews()

        weekdays.forEach { weekday ->
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }

            val textView = TextView(requireContext()).apply {
                layoutParams = params
                text = weekday
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.body_1))
                includeFontPadding = false

                typeface = try {
                    ResourcesCompat.getFont(requireContext(), R.font.pretendard_semibold)
                        ?: Typeface.DEFAULT
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
            binding.llCalendarDate.addView(textView)
        }
    }

    /**
     * 캘린더 RecyclerView 설정 - 성능 최적화
     */
    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarAdapter { day ->
            viewModel.selectDay(day)
            // 날짜 클릭 시 바텀시트 표시
            showScheduleBottomSheet(day)
        }

        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            setHasFixedSize(true)

            // 성능 최적화
            itemAnimator = null
            setItemViewCacheSize(42)
            isNestedScrollingEnabled = false
            recycledViewPool.setMaxRecycledViews(0, 50)

            // 수정: ViewTreeObserver를 사용하여 높이 계산
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (height > 0 && !isItemDecorationAdded) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        adjustRecyclerViewHeight(this@apply)
                    }
                }
            })
        }
    }

    /**
     * RecyclerView 높이 동적 조정
     * 수정: ItemDecoration을 한 번만 추가하도록 수정
     */
    private fun adjustRecyclerViewHeight(recyclerView: RecyclerView) {
        // 이미 추가되었다면 return
        if (isItemDecorationAdded) return

        val cardView = binding.cvCalendarCard
        val cardPaddingVertical = dpToPx(20) * 2  // 위아래 패딩
        val weekdayHeaderHeight = binding.llCalendarDate.height
        val availableHeight = cardView.height - cardPaddingVertical - weekdayHeaderHeight

        // 6주 기준으로 아이템 높이 계산
        val rowCount = 6
        val itemHeight = availableHeight / rowCount

        // RecyclerView 높이 설정
        recyclerView.layoutParams = recyclerView.layoutParams.apply {
            height = availableHeight
        }

        // ItemDecoration을 한 번만 추가
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                view.layoutParams.height = itemHeight
            }
        })

        isItemDecorationAdded = true  // 플래그 설정

        // RecyclerView 갱신
        recyclerView.requestLayout()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * 클릭 리스너 설정
     */
    private fun setupClickListeners() {
        binding.ivCalendarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.ivMonthBefore.setOnClickListener {
            viewModel.goToPreviousMonth()
        }

        binding.ivMonthAfter.setOnClickListener {
            viewModel.goToNextMonth()
        }

        // 일정 추가 화면으로 이동 (바텀시트는 날짜/필터 클릭 시에만 표시)
        binding.btnCalendarManage.setOnClickListener {
            showAddEventDialog()
        }
    }

    /**
     * ViewModel 관찰 - StateFlow 사용
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    /**
     * UI 업데이트
     */
    private fun updateUi(state: CalendarUiState) {
        // 달력 업데이트 - 항상 업데이트하여 초기화 시에도 표시되도록 함
        calendarAdapter.updateDays(state.calendarDays)
        binding.tvYearMonthLabel.text = viewModel.getYearMonthText()

        // 펫 목록 업데이트
        if (state.pets.isNotEmpty()) {
            petFilterAdapter.updatePets(state.pets)
        }

        // 선택된 펫 업데이트
        petFilterAdapter.selectPet(state.selectedPet)

        // 에러 처리
        state.error?.let {
            showSnackbar(it)
            viewModel.clearError()
        }
    }

    /**
     * 캘린더 권한 확인
     */
    private fun checkCalendarPermission() {
        when {
            hasCalendarPermissions() -> {
                loadDataSequentially()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR) -> {
                Snackbar.make(
                    binding.root,
                    "일정을 관리하려면 캘린더 권한이 필요합니다.",
                    Snackbar.LENGTH_LONG
                ).setAction("허용") {
                    requestPermissions()
                }.show()
            }
            else -> {
                requestPermissions()
            }
        }
    }

    /**
     * 캘린더 권한 확인
     */
    private fun hasCalendarPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 권한 요청
     */
    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            )
        )
    }

    /**
     * 일정 바텀시트 표시
     */
    private fun showScheduleBottomSheet(day: CalendarDay? = null) {
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_calendar, null)

        val tvDate = bottomSheetView.findViewById<TextView>(R.id.tv_bottom_sheet_date)
        val selectedDay = day ?: viewModel.uiState.value.selectedDate
        tvDate.text = formatDate(selectedDay)

        val rvSchedules = bottomSheetView.findViewById<RecyclerView>(R.id.rv_bottom_sheet_schedules)
        val tvNoSchedule = bottomSheetView.findViewById<TextView>(R.id.tv_no_schedule)

        val events = selectedDay?.let { viewModel.getEventsForDay(it) } ?: emptyList()

        if (events.isNotEmpty()) {
            rvSchedules.visibility = View.VISIBLE
            tvNoSchedule.visibility = View.GONE

            scheduleAdapter = ScheduleBottomSheetAdapter(events)
            rvSchedules.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = scheduleAdapter
                setHasFixedSize(true)
            }
        } else {
            rvSchedules.visibility = View.GONE
            tvNoSchedule.visibility = View.VISIBLE
        }

        bottomSheetDialog?.setContentView(bottomSheetView)

        bottomSheetDialog?.window?.apply {
            setDimAmount(0.5f)
        }

        bottomSheetDialog?.show()
    }

    /**
     * 날짜 포맷팅
     */
    private fun formatDate(day: CalendarDay?): String {
        if (day == null) return ""

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, day.year)
            set(Calendar.MONTH, day.month)
            set(Calendar.DAY_OF_MONTH, day.day)
        }

        val monthFormat = SimpleDateFormat("M월 d일", Locale.KOREAN)
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.KOREAN)

        return "${monthFormat.format(calendar.time)} ${dayOfWeekFormat.format(calendar.time)}"
    }

    /**
     * 일정 추가 화면으로 이동
     */
    private fun showAddEventDialog() {
        findNavController().navigate(
            R.id.action_calendarFragment_to_calendarAlarmFragment
        )
    }

    /**
     * Snackbar 표시 헬퍼
     */
    private fun showSnackbar(message: String, duration: Int = Snackbar.LENGTH_LONG) {
        _binding?.let {
            Snackbar.make(it.root, message, duration).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // 리소스 정리
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        scheduleAdapter = null

        // 플래그 초기화
        isItemDecorationAdded = false

        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = CalendarFragment()
    }
}