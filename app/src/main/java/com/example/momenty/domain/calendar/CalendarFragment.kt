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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCalendarBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var petFilterAdapter: PetFilterAdapter

    // lazy 초기화를 지연 초기화로 변경
    private var deviceCalendarHelper: DeviceCalendarHelper? = null
    private var repository: CalendarRepository? = null

    private val viewModel: CalendarViewModel by viewModels {
        // Repository를 안전하게 초기화
        val helper = deviceCalendarHelper ?: DeviceCalendarHelper(requireContext()).also {
            deviceCalendarHelper = it
        }
        val repo = repository ?: CalendarRepository(
            apiService = RetrofitClient.calendarApiService,
            deviceCalendarHelper = helper
        ).also {
            repository = it
        }
        CalendarViewModelFactory(repo)
    }

    // 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false

        if (readGranted && writeGranted) {
            // 순차적으로 로드하여 부하 분산
            loadDataSequentially()
        } else {
            Snackbar.make(
                binding.root,
                "캘린더 권한이 필요합니다.",
                Snackbar.LENGTH_LONG
            ).show()
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

        // UI 먼저 초기화 (가벼운 작업)
        setupWeekdayHeader()
        setupCalendarRecyclerView()
        setupPetFilter()
        setupClickListeners()
        observeViewModel()

        // 권한 확인은 마지막에
        checkCalendarPermission()
    }

    /**
     * 데이터를 순차적으로 로드하여 ANR 방지
     */
    private fun loadDataSequentially() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. 먼저 더미 펫 데이터 표시 (즉시)
                // viewModel.loadDummyPets()는 init에서 이미 실행됨

                // 2. 캘린더 목록 로드 (백그라운드)
                delay(100) // UI 렌더링 시간 확보
                viewModel.loadAvailableCalendars()

                // 3. 캘린더 데이터 로드 (백그라운드)
                delay(100)
                viewModel.loadCalendar()

                // 4. 서버에서 펫 데이터 로드 (선택사항, 백그라운드)
                delay(100)
                viewModel.loadPets()

            } catch (e: Exception) {
                Snackbar.make(
                    binding.root,
                    "데이터 로드 중 오류가 발생했습니다: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * 반려동물 필터 설정
     */
    private fun setupPetFilter() {
        petFilterAdapter = PetFilterAdapter(emptyList()) { pet ->
            viewModel.selectPetFilter(pet)

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
            // 스크롤 성능 최적화
            isNestedScrollingEnabled = false
        }
    }

    /**
     * 요일 헤더 설정
     */
    private fun setupWeekdayHeader() {
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.body_1))
                includeFontPadding = false

                typeface = try {
                    ResourcesCompat.getFont(requireContext(), R.font.pretendard_semibold)
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
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            viewModel.selectDay(day)
        }

        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            setHasFixedSize(false) // 동적 높이를 위해 false로 변경
            // 성능 최적화
            itemAnimator = null // 애니메이션 비활성화
            setItemViewCacheSize(42) // 6주 * 7일
            isNestedScrollingEnabled = false

            // RecyclerView가 측정된 후 높이 조정
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    adjustCalendarHeight()
                }
            })
        }
    }

    /**
     * 캘린더 높이를 카드뷰에 맞게 조정
     */
    private fun adjustCalendarHeight() {
        val cardView = binding.cvCalendarCard
        val weekdayHeader = binding.llCalendarDate
        val recyclerView = binding.calendarRecyclerView

        // 카드뷰 내부 사용 가능한 높이 계산
        val cardPaddingVertical = dpToPx(44) // 20dp(top) + 24dp(bottom)
        val weekdayHeight = weekdayHeader.height
        val weekdayMarginBottom = dpToPx(15)

        val availableHeight = cardView.height - cardPaddingVertical - weekdayHeight - weekdayMarginBottom

        // 6주 기준으로 각 행의 높이 계산
        val rowCount = 6
        val itemHeight = availableHeight / rowCount

        // RecyclerView 높이 설정
        recyclerView.layoutParams = recyclerView.layoutParams.apply {
            height = availableHeight
        }

        // 각 아이템 높이를 동적으로 설정
        recyclerView.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: View,
                parent: androidx.recyclerview.widget.RecyclerView,
                state: androidx.recyclerview.widget.RecyclerView.State
            ) {
                view.layoutParams.height = itemHeight
            }
        })
    }

    /**
     * dp를 px로 변환
     */
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

        binding.ivMonthDefore.setOnClickListener {
            viewModel.goToPreviousMonth()
        }

        binding.ivMonthAtfer.setOnClickListener {
            viewModel.goToNextMonth()
        }

        binding.btnCalendarManage.setOnClickListener {
            showEventDialog()
        }

        binding.ivCalendarAdd.setOnClickListener {
            showAddEventDialog()
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        viewModel.calendarDays.observe(viewLifecycleOwner) { days ->
            calendarAdapter.updateDays(days)
            binding.tvYearMonthLabel.text = viewModel.getYearMonthText()
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { selectedDay ->
            selectedDay?.let {
                showEventsForDay(it)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.availableCalendars.observe(viewLifecycleOwner) { calendars ->
            // 필요시 캘린더 선택 UI 표시
        }

        viewModel.pets.observe(viewLifecycleOwner) { pets ->
            petFilterAdapter.updatePets(pets)
        }

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
        Snackbar.make(binding.root, "일정 관리 기능 구현 예정", Snackbar.LENGTH_SHORT).show()
    }

    /**
     * 일정 추가 다이얼로그 표시
     */
    private fun showAddEventDialog() {
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
        @JvmStatic
        fun newInstance() = CalendarFragment()
    }
}