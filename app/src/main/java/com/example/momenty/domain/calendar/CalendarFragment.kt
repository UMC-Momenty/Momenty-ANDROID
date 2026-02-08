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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCalendarBinding
import com.google.android.material.snackbar.Snackbar

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var petFilterAdapter: PetFilterAdapter
    private lateinit var deviceCalendarHelper: DeviceCalendarHelper
    private lateinit var repository: CalendarRepository

    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory(repository)
    }

    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false

        if (readGranted && writeGranted) {
            viewModel.loadAvailableCalendars()
            viewModel.loadCalendar()
            viewModel.loadPets()
        } else {
            Snackbar.make(
                binding.root,
                "캘린더 권한이 필요합니다.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceCalendarHelper = DeviceCalendarHelper(requireContext())

        repository = CalendarRepository(
            apiService = RetrofitClient.calendarApiService,
            deviceCalendarHelper = deviceCalendarHelper
        )
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

        setupPetFilter()
        setupWeekdayHeader()
        setupCalendarRecyclerView()
        setupClickListeners()
        observeViewModel()
        checkCalendarPermission()
    }

    /**
     * 반려동물 필터 설정
     */
    private fun setupPetFilter()    {
        petFilterAdapter = PetFilterAdapter(emptyList()) { pet ->
            // pet == null : 전체 일정 / or 선택된 반려동물 일정
            viewModel.selectPetFilter(pet)

            // 사용자 피드백
            val message = if (pet == null) {
                "전체 일정을 표시합니다"
            } else {
                "${pet.name}의 일정을 표시합니다"
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
        }

        binding.rvPetFilter.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = petFilterAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 요일 헤더 설정
     */
    private fun setupWeekdayHeader()    {
        val weekdays = resources.getStringArray(R.array.calendar_date)
        binding.llCalendarDate.removeAllViews()

        weekdays.forEachIndexed { index, weekday ->
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(R.color.body_1)
                includeFontPadding = false

                typeface = try {
                    ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
                } catch (e: Exception) {
                    Typeface.DEFAULT
                }
            }
            binding.llCalendarDate.addView(textView)
        }
    }

    /**
     * 캘린더 RecyclerView 설정
     */
    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarAdapter(emptyList())  { day ->
            viewModel.selectDay(day)
        }

        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 클릭 리스너 설정
     */
    private fun setupClickListeners() {
        // 뒤로가기 버튼
        binding.ivCalendarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // 이전 달 버튼
        binding.ivMonthDefore.setOnClickListener {
            viewModel.goToPreviousMonth()
        }

        // 다음 달 버튼
        binding.ivMonthAtfer.setOnClickListener {
            viewModel.goToNextMonth()
        }

        // 일정 관리 버튼
        binding.btnCalendarManage.setOnClickListener {
            showEventDialog()
        }

        // 일정 추가 버튼
        binding.ivCalendarAdd.setOnClickListener {
            showAddEventDialog()
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 달력 날짜 데이터
        viewModel.calendarDays.observe(viewLifecycleOwner) { days ->
            calendarAdapter.updateDays(days)
            binding.tvYearMonthLabel.text = viewModel.getYearMonthText()
        }

        // 선택된 날짜
        viewModel.selectedDate.observe(viewLifecycleOwner) { selectedDay ->
            selectedDay?.let {
                showEventsForDay(it)
            }
        }

        // 에러 메시지
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        // 사용 가능한 캘린더 목록
        viewModel.availableCalendars.observe(viewLifecycleOwner) { calendars ->
            // 필요시 캘린더 선택 UI 표시
        }

        // 반려동물 목록
        viewModel.pets.observe(viewLifecycleOwner) { pets ->
            petFilterAdapter.updatePets(pets)
        }

        // 선택된 반려동물
        viewModel.selectedPet.observe(viewLifecycleOwner) { pet ->
            petFilterAdapter.selectPet(pet)
        }
    }

    /**
     * 캘린더 권한 확인
     */
    private fun checkCalendarPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용됨
                viewModel.loadAvailableCalendars()
                viewModel.loadCalendar()
                viewModel.loadPets()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR) -> {
                // 권한 요청 이유 설명
                Snackbar.make(
                    binding.root,
                    "일정을 관리하려면 캘린더 권한이 필요합니다.",
                    Snackbar.LENGTH_LONG
                ).setAction("허용") {
                    requestPermissions()
                }.show()
            }
            else -> {
                // 권한 요청
                requestPermissions()
            }
        }
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
     * 특정 날짜의 일정 표시
     */
    private fun showEventsForDay(day: CalendarDay) {
        val events = viewModel.getEventsForDay(day)

        if (events.isNotEmpty()) {
            // TODO: 이벤트 목록을 보여주는 바텀시트나 다이얼로그 구현
            val eventTitles = events.joinToString("\n") {
                "• ${it.title} (${formatTime(it.startTime)})"
            }
            Snackbar.make(binding.root, eventTitles, Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(binding.root, "일정이 없습니다", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * 일정 관리 다이얼로그 표시
     */
    private fun showEventDialog() {
        // TODO: 일정 관리 화면 또는 다이얼로그 구현
        Snackbar.make(binding.root, "일정 관리 기능 구현 예정", Snackbar.LENGTH_SHORT).show()
    }

    /**
     * 일정 추가 다이얼로그 표시
     */
    private fun showAddEventDialog() {
        // TODO: 일정 추가 다이얼로그 구현
        Snackbar.make(binding.root, "일정 추가 기능 구현 예정", Snackbar.LENGTH_SHORT).show()
    }

    /**
     * 시간 포맷팅
     */
    private fun formatTime(date: java.util.Date): String {
        val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Fragment 인스턴스 생성
         */
        @JvmStatic
        fun newInstance() = CalendarFragment()
    }
}