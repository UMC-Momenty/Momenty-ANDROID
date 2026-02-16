package com.example.momenty.domain.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.domain.calendar.api.request.CreateScheduleRequest
import com.example.momenty.global.security.LocalDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val calendarLock = Any()
    private var currentCalendar = Calendar.getInstance()
    private val eventsMap = ConcurrentHashMap<String, MutableList<CalendarEvent>>()

    private var localDataManager: LocalDataManager? = null

    companion object {
        private const val CALENDAR_GRID_SIZE = 42
        private const val TAG = "CalendarViewModel"
    }

    init {
        // ViewModel 생성 시 자동으로 초기화
        Log.d(TAG, "ViewModel init block called")
        initialize()
    }

    fun setLocalDataManager(manager: LocalDataManager) {
        this.localDataManager = manager
    }

    fun initialize() {
        Log.d(TAG, "initialize() called")
        viewModelScope.launch {
            val today = Calendar.getInstance()

            // 현재 달력 초기화 - 이게 핵심!
            synchronized(calendarLock) {
                currentCalendar = today.clone() as Calendar
            }

            val initialSelectedDate = CalendarDay(
                day = today.get(Calendar.DAY_OF_MONTH),
                month = today.get(Calendar.MONTH),
                year = today.get(Calendar.YEAR),
                isCurrentMonth = true,
                isToday = true,
                isSelected = true,
                events = getEventsForDate(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                )
            )

            _uiState.update {
                it.copy(
                    selectedPet = it.selectedPet ?: null,
                    selectedDate = initialSelectedDate
                )
            }

            // 달력 날짜 생성
            Log.d(TAG, "Calling generateCalendarDays()")
            generateCalendarDays()
        }
    }

    /**
     * ✅ 일정 생성 API 호출
     * @param petId 반려동물 ID (Long)
     * @param request 일정 생성 요청 데이터
     * @return 성공 여부
     */
    suspend fun createSchedule(petId: Long, request: CreateScheduleRequest): Boolean {
        return try {
            Log.d(TAG, "Creating schedule for pet: $petId, title: ${request.title}")

            withContext(Dispatchers.IO) {
                val response = repository.createSchedule(petId, request)

                if (response != null) {
                    // 성공 시 true 반환
                    true
                } else {
                    Log.e(TAG, "Failed to create schedule: response is null")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while creating schedule", e)
            _uiState.update { it.copy(error = "일정 생성 실패: ${e.message}") }
            false
        }
    }

    /**
     * ✅ LocalDataManager에서 일정 로드 및 달력에 반영
     */
    fun loadSchedules() {
        Log.d(TAG, "loadSchedules() called")
        viewModelScope.launch {
            try {
                val manager = localDataManager
                if (manager == null) {
                    Log.w(TAG, "LocalDataManager not set, cannot load schedules")
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true) }

                // ✅ getSchedules() 사용
                val schedules = withContext(Dispatchers.IO) {
                    manager.getSchedules()
                }

                Log.d(TAG, "Loaded ${schedules.size} schedules from LocalDataManager")

                eventsMap.clear()

                schedules.forEach { schedule ->
                    val calendarEvent = convertToCalendarEvent(schedule)
                    if (calendarEvent != null) {
                        val dateKey =
                            if (schedule.isOneTime && schedule.date != null) {
                                schedule.date
                            } else {
                                null
                            }

                        if (dateKey != null) {
                            eventsMap.getOrPut(dateKey) { mutableListOf() }.add(calendarEvent)
                            Log.d(TAG, "Added event: ${calendarEvent.title} for date: $dateKey")
                        } else if (!schedule.isOneTime && schedule.repeatDays != null) {
                            // 반복성 일정: 현재 월의 해당 요일들에 모두 추가
                            addRepeatEventToMonth(calendarEvent, schedule.repeatDays)
                        }
                    }
                }

                Log.d(TAG, "Total events in map: ${eventsMap.size} dates")

                refreshCalendarDays()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load schedules", e)
                _uiState.update {
                    it.copy(
                        error = "일정 로드 실패: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * ✅ 반복 일정을 현재 월의 해당 요일들에 추가
     */
    private fun addRepeatEventToMonth(event: CalendarEvent, repeatDayNames: List<String>) {
        synchronized(calendarLock) {
            val calendar = currentCalendar.clone() as Calendar
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            // 해당 월의 첫 날로 이동
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // 월의 모든 날짜를 순회
            for (day in 1..maxDay) {
                calendar.set(Calendar.DAY_OF_MONTH, day)
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                // 해당 요일이 repeatDays에 포함되는지 확인
                val dayName = convertCalendarDayToName(dayOfWeek)
                if (repeatDayNames.contains(dayName)) {
                    val dateKey = String.format("%04d-%02d-%02d", year, month + 1, day)
                    eventsMap.getOrPut(dateKey) { mutableListOf() }.add(event)
                    Log.d(TAG, "Added repeat event: ${event.title} for date: $dateKey")
                }
            }
        }
    }

    /**
     * Calendar.DAY_OF_WEEK를 DayOfWeek enum 이름으로 변환
     */
    private fun convertCalendarDayToName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "SUNDAY"
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            else -> "MONDAY"
        }
    }

    /**
     * ✅ LocalSchedule을 CalendarEvent로 변환
     */
    private fun convertToCalendarEvent(schedule: com.example.momenty.global.mock.LocalSchedule): CalendarEvent? {
        return try {
            // startAt 형식: "YYYY-MM-DDTHH:mm:ss"
            val startAt = if (schedule.isOneTime && schedule.date != null) {
                "${schedule.date}T${schedule.time}:00"
            } else {
                // 반복성 일정은 오늘 날짜 기준
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date())
                "${today}T${schedule.time}:00"
            }

            CalendarEvent(
                scheduleId = schedule.scheduleId,
                title = schedule.title,
                category = schedule.category,
                startAt = startAt,
                memo = schedule.memo,  // ✅ memo 필드 추가
                durationMinutes = schedule.durationMinutes,  // ✅ durationMinutes 필드 추가
                petId = schedule.petId,
                isAlarmEnabled = schedule.isAlarmEnabled
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error converting schedule to event", e)
            null
        }
    }

    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    // CalendarEvent는 startAt(String)을 사용하므로 Date로 변환 필요
                    val date = parseStartAtToDate(event.startAt)
                    val dateKey = DateUtils.getDateKey(date)

                    Log.d(TAG, "Adding event: ${event.title} for date: $dateKey")

                    eventsMap.getOrPut(dateKey) { mutableListOf() }.add(event)

                    Log.d(TAG, "Total events for $dateKey: ${eventsMap[dateKey]?.size}")
                }

                refreshCalendarDays()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add event", e)
                _uiState.update { it.copy(error = "이벤트 추가 실패: ${e.message}") }
            }
        }
    }

    fun addEvents(events: List<CalendarEvent>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    events.forEach { event ->
                        val date = parseStartAtToDate(event.startAt)
                        val dateKey = DateUtils.getDateKey(date)
                        eventsMap.getOrPut(dateKey) { mutableListOf() }.add(event)
                    }
                }

                refreshCalendarDays()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add events", e)
                _uiState.update { it.copy(error = "이벤트 추가 실패") }
            }
        }
    }

    fun removeEvent(event: CalendarEvent) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val date = parseStartAtToDate(event.startAt)
                    val dateKey = DateUtils.getDateKey(date)
                    eventsMap[dateKey]?.remove(event)
                    if (eventsMap[dateKey]?.isEmpty() == true) {
                        eventsMap.remove(dateKey)
                    }
                }
                refreshCalendarDays()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove event", e)
                _uiState.update { it.copy(error = "이벤트 삭제 실패") }
            }
        }
    }

    /**
     * ISO 8601 형식의 startAt을 Date로 변환
     */
    private fun parseStartAtToDate(startAt: String): Date {
        return try {
            // "YYYY-MM-DDTHH:mm:ss" 형식 파싱
            val parts = startAt.split("T")
            if (parts.size == 2) {
                val dateParts = parts[0].split("-")
                val timeParts = parts[1].split(":")

                Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                    set(Calendar.MINUTE, timeParts[1].toInt())
                    set(Calendar.SECOND, if (timeParts.size > 2) timeParts[2].toInt() else 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
            } else {
                Date()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing startAt: $startAt", e)
            Date()
        }
    }

    fun selectDay(day: CalendarDay) {
        viewModelScope.launch {
            try {
                val updatedDays = withContext(Dispatchers.Default) {
                    _uiState.value.calendarDays.map { calendarDay ->
                        calendarDay.copy(
                            isSelected = calendarDay.day == day.day &&
                                    calendarDay.month == day.month &&
                                    calendarDay.year == day.year,
                            isToday = calendarDay.isToday
                        )
                    }
                }

                _uiState.update {
                    it.copy(
                        calendarDays = updatedDays,
                        selectedDate = updatedDays.find { it.isSelected }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to select day", e)
                _uiState.update { it.copy(error = "날짜 선택 실패") }
            }
        }
    }

    private fun generateCalendarDays() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "generateCalendarDays started")
                val days = withContext(Dispatchers.Default) {
                    calculateCalendarDays()
                }
                Log.d(TAG, "Generated ${days.size} calendar days")
                _uiState.update { it.copy(calendarDays = days, error = null) }
                Log.d(TAG, "UI state updated with calendar days")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate calendar", e)
                _uiState.update { it.copy(error = "달력 생성 실패") }
            }
        }
    }

    private suspend fun calculateCalendarDays(): List<CalendarDay> = withContext(Dispatchers.Default) {
        val days = mutableListOf<CalendarDay>()

        val (year, month, firstDayOfWeek, daysInMonth) = synchronized(calendarLock) {
            val calendar = currentCalendar.clone() as Calendar
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            CalendarInfo(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),
                firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
        }

        val today = Calendar.getInstance()
        val currentSelectedDate = _uiState.value.selectedDate

        addPreviousMonthDays(days, year, month, firstDayOfWeek)
        addCurrentMonthDays(days, year, month, daysInMonth, today, currentSelectedDate)
        addNextMonthDays(days, year, month)

        days
    }

    private fun addPreviousMonthDays(
        days: MutableList<CalendarDay>,
        currentYear: Int,
        currentMonth: Int,
        firstDayOfWeek: Int
    ) {
        val prevMonth = if (currentMonth == Calendar.JANUARY) Calendar.DECEMBER else currentMonth - 1
        val prevYear = if (currentMonth == Calendar.JANUARY) currentYear - 1 else currentYear

        val prevMonthCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, prevYear)
            set(Calendar.MONTH, prevMonth)
        }
        val daysInPrevMonth = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val emptyDays = firstDayOfWeek - 1
        for (i in 0 until emptyDays) {
            val day = daysInPrevMonth - emptyDays + i + 1
            days.add(
                CalendarDay(
                    day = day,
                    month = prevMonth,
                    year = prevYear,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    events = emptyList()
                )
            )
        }
    }

    private fun addCurrentMonthDays(
        days: MutableList<CalendarDay>,
        year: Int,
        month: Int,
        daysInMonth: Int,
        today: Calendar,
        currentSelectedDate: CalendarDay?
    ) {
        val effectiveSelectedDate = currentSelectedDate ?: CalendarDay(
            day = today.get(Calendar.DAY_OF_MONTH),
            month = today.get(Calendar.MONTH),
            year = today.get(Calendar.YEAR),
            isCurrentMonth = true,
            isToday = true,
            isSelected = true,
            events = getEventsForDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        )

        for (day in 1..daysInMonth) {
            val isToday = today.get(Calendar.YEAR) == year &&
                    today.get(Calendar.MONTH) == month &&
                    today.get(Calendar.DAY_OF_MONTH) == day

            val isSelected = effectiveSelectedDate.let {
                it.day == day && it.month == month && it.year == year
            }

            val eventsForDay = getEventsForDate(year, month, day)

            days.add(
                CalendarDay(
                    day = day,
                    month = month,
                    year = year,
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected,
                    events = eventsForDay
                )
            )
        }
    }

    private fun addNextMonthDays(
        days: MutableList<CalendarDay>,
        currentYear: Int,
        currentMonth: Int
    ) {
        val nextMonth = if (currentMonth == Calendar.DECEMBER) Calendar.JANUARY else currentMonth + 1
        val nextYear = if (currentMonth == Calendar.DECEMBER) currentYear + 1 else currentYear

        val remainingDays = CALENDAR_GRID_SIZE - days.size
        for (i in 1..remainingDays) {
            days.add(
                CalendarDay(
                    day = i,
                    month = nextMonth,
                    year = nextYear,
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false,
                    events = emptyList()
                )
            )
        }
    }

    private fun refreshCalendarDays() {
        generateCalendarDays()
    }

    // ✅ 월 변경 시 loadSchedules() 호출 추가
    fun goToPreviousMonth() {
        synchronized(calendarLock) { currentCalendar.add(Calendar.MONTH, -1) }
        loadSchedules()
    }

    fun goToNextMonth() {
        synchronized(calendarLock) { currentCalendar.add(Calendar.MONTH, 1) }
        loadSchedules()
    }

    fun getYearMonthText(): String = synchronized(calendarLock) {
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH) + 1
        "${year}년 ${month}월"
    }

    fun getEventsForDay(day: CalendarDay): List<CalendarEvent> =
        getEventsForDate(day.year, day.month, day.day)

    private fun getEventsForDate(year: Int, month: Int, day: Int): List<CalendarEvent> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val dateKey = DateUtils.getDateKey(calendar.time)
        val allEvents = eventsMap[dateKey]?.toList() ?: emptyList()
        val selectedPetId = _uiState.value.selectedPet?.petId
        return if (selectedPetId != null) allEvents.filter { it.petId == selectedPetId } else allEvents
    }

    fun loadPets() {
        Log.d(TAG, "loadPets() called")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                Log.d(TAG, "Calling repository.getPets()")
                val pets = withContext(Dispatchers.IO) { repository.getPets() }
                Log.d(TAG, "Repository returned ${pets.size} pets")
                _uiState.update { it.copy(pets = pets, isLoading = false) }
                Log.d(TAG, "UI state updated with ${pets.size} pets")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load pets", e)
                _uiState.update { it.copy(error = "반려동물 정보 로드 실패: ${e.message}", isLoading = false) }
            }
        }
    }

    fun selectPetFilter(pet: Pet?) {
        _uiState.update { it.copy(selectedPet = pet) }
        val selected = _uiState.value.selectedDate
        if (selected != null) {
            val updatedEvents = getEventsForDate(selected.year, selected.month, selected.day)
            _uiState.update { it.copy(selectedDate = selected.copy(events = updatedEvents)) }
        }
        generateCalendarDays()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) { eventsMap.clear() }
                refreshCalendarDays()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear events", e)
                _uiState.update { it.copy(error = "이벤트 삭제 실패") }
            }
        }
    }
}

data class CalendarUiState(
    val calendarDays: List<CalendarDay> = emptyList(),
    val selectedDate: CalendarDay? = null,
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

private data class CalendarInfo(
    val year: Int,
    val month: Int,
    val firstDayOfWeek: Int,
    val daysInMonth: Int
)

object DateUtils {
    fun normalizeDate(date: Date): Date {
        val cal = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }

    fun getDateKey(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}