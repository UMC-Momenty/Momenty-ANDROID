package com.example.momenty.domain.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.momenty.R
import com.example.momenty.databinding.FragmentCalendarBinding
import com.example.momenty.global.security.TokenManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var petFilterAdapter: PetFilterAdapter

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var scheduleAdapter: ScheduleBottomSheetAdapter? = null

    @Inject
    lateinit var tokenManager: TokenManager

    private val viewModel: CalendarViewModel by activityViewModels {
        val helper = DeviceCalendarHelper(requireContext())
        val repo = CalendarRepository(
            calendarApiService = RetrofitClient.calendarApiService,
            petApiService = RetrofitClient.petApiService,
            // deviceCalendarHelper = helper,
            tokenManager = tokenManager
        )
        CalendarViewModelFactory(repo)
    }

    /*private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false

            if (readGranted && writeGranted) {
                loadDataSequentially()
            } else {
                showSnackbar("캘린더 권한이 필요합니다.")
            }
        }*/

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

        RetrofitClient.initialize(tokenManager)

        setupWeekdayHeader()
        setupCalendarRecyclerView()
        setupPetFilter()
        setupClickListeners()

        // ViewModel 관찰 시작
        observeViewModel()

        // 반려동물 데이터 로드 (달력은 init에서 자동 초기화됨)
        viewModel.loadPets()
        // checkCalendarPermission()
    }

    /**
     * RecyclerView 설정
     */
    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarAdapter { day ->
            viewModel.selectDay(day)
            showScheduleBottomSheet(day)
        }

        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    /**
     * 반려동물 필터
     */
    private fun setupPetFilter() {
        petFilterAdapter = PetFilterAdapter(
            pets = emptyList(),
            multiSelect = false,
            onPetClick = { pet ->
                viewModel.selectPetFilter(pet)
                showScheduleBottomSheet()
            }
        )

        binding.rvPetFilter.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = petFilterAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * 요일 헤더
     */
    private fun setupWeekdayHeader() {
        val weekdays = resources.getStringArray(R.array.calendar_date)
        binding.llCalendarDate.removeAllViews()

        weekdays.forEach { weekday ->
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { weight = 1f }

            val textView = TextView(requireContext()).apply {
                layoutParams = params
                text = weekday
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.body_1))
                includeFontPadding = false
                typeface = ResourcesCompat.getFont(
                    requireContext(),
                    R.font.pretendard_semibold
                ) ?: Typeface.DEFAULT
            }

            binding.llCalendarDate.addView(textView)
        }
    }

    /**
     * ViewModel 관찰
     */
    private fun observeViewModel() {
        // 즉시 현재 상태로 UI 업데이트 (데이터가 있을 때만)
        val currentState = viewModel.uiState.value
        android.util.Log.d("CalendarFragment", "Initial state has ${currentState.calendarDays.size} days")

        if (currentState.calendarDays.isNotEmpty()) {
            calendarAdapter.updateDays(currentState.calendarDays)
            binding.tvYearMonthLabel.text = viewModel.getYearMonthText()
        }

        if (currentState.pets.isNotEmpty()) {
            petFilterAdapter.updatePets(currentState.pets)
            petFilterAdapter.selectPet(currentState.selectedPet)
        }

        // 이후 변경사항 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                android.util.Log.d("CalendarFragment", "Started collecting uiState")
                viewModel.uiState.collect { state ->
                    android.util.Log.d("CalendarFragment", "Received state with ${state.calendarDays.size} days, ${state.pets.size} pets")

                    if (state.calendarDays.isNotEmpty()) {
                        calendarAdapter.updateDays(state.calendarDays)
                        binding.tvYearMonthLabel.text = viewModel.getYearMonthText()
                    }

                    if (state.pets.isNotEmpty()) {
                        petFilterAdapter.updatePets(state.pets)
                        petFilterAdapter.selectPet(state.selectedPet)
                    }

                    state.error?.let {
                        showSnackbar(it)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    /**
     * 데이터 로드
     */
    private fun loadDataSequentially() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadAvailableCalendars()
        }
    }

    /**
     * 권한 체크
     * private fun checkCalendarPermission() {
     *         if (hasCalendarPermissions()) {
     *             loadDataSequentially()
     *         } else {
     *             requestPermissionLauncher.launch(
     *                 arrayOf(
     *                     Manifest.permission.READ_CALENDAR,
     *                     Manifest.permission.WRITE_CALENDAR
     *                 )
     *             )
     *         }
     *     }
     *
     *     private fun hasCalendarPermissions(): Boolean {
     *         return ContextCompat.checkSelfPermission(
     *             requireContext(),
     *             Manifest.permission.READ_CALENDAR
     *         ) == PackageManager.PERMISSION_GRANTED &&
     *                 ContextCompat.checkSelfPermission(
     *                     requireContext(),
     *                     Manifest.permission.WRITE_CALENDAR
     *                 ) == PackageManager.PERMISSION_GRANTED
     *     }
     */


    /**
     * 바텀시트
     */
    private fun showScheduleBottomSheet(day: CalendarDay? = null) {
        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_calendar, null)

        val selectedDay = day ?: viewModel.uiState.value.selectedDate
        val tvDate = view.findViewById<TextView>(R.id.tv_bottom_sheet_date)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_bottom_sheet_schedules)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_no_schedule)

        tvDate.text = formatDate(selectedDay)

        val events = selectedDay?.let { viewModel.getEventsForDay(it) } ?: emptyList()

        if (events.isNotEmpty()) {
            scheduleAdapter = ScheduleBottomSheetAdapter(events)
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.adapter = scheduleAdapter
            tvEmpty.visibility = View.GONE
        } else {
            rv.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }

        bottomSheetDialog?.setContentView(view)
        bottomSheetDialog?.show()
    }

    private fun formatDate(day: CalendarDay?): String {
        if (day == null) return ""

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, day.year)
            set(Calendar.MONTH, day.month)
            set(Calendar.DAY_OF_MONTH, day.day)
        }

        val monthFormat = SimpleDateFormat("M월 d일", Locale.KOREAN)
        val dayFormat = SimpleDateFormat("EEEE", Locale.KOREAN)

        return "${monthFormat.format(calendar.time)} ${dayFormat.format(calendar.time)}"
    }

    private fun setupClickListeners() {
        binding.ivCalendarBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.ivMonthBefore.setOnClickListener { viewModel.goToPreviousMonth() }
        binding.ivMonthAfter.setOnClickListener { viewModel.goToNextMonth() }

        binding.btnCalendarManage.setOnClickListener {
            findNavController().navigate(
                R.id.action_calendarFragment_to_calendarAlarmFragment
            )
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        bottomSheetDialog?.dismiss()
        bottomSheetDialog = null
        scheduleAdapter = null
        _binding = null
        super.onDestroyView()
    }
}