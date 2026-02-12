package com.example.momenty.domain.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val calendarLock = Any()
    private var currentCalendar = Calendar.getInstance()

    // ConcurrentHashMap으로 변경하여 더 나은 동시성 지원
    private val eventsMap = ConcurrentHashMap<String, MutableList<CalendarEvent>>()

    companion object {
        private const val CALENDAR_GRID_SIZE = 42 // 6 weeks × 7 days
        private const val TAG = "CalendarViewModel"
    }

    fun initialize() {
        viewModelScope.launch {
            // Update state immediately with dummy data (lightweight)
            _uiState.update { it.copy(pets = getDummyPets()) }

            // Defer calendar generation slightly
            delay(50)
            generateCalendarDays()
        }
    }

    private fun getDummyPets(): List<Pet> {
        return listOf(
            Pet(id = "1", name = "코코", imageUrl = null, color = null),
            Pet(id = "2", name = "몽이", imageUrl = null, color = null),
            Pet(id = "3", name = "초코", imageUrl = null, color = null)
        )
    }

    /**
     * 이벤트 추가 - ANR 방지를 위해 백그라운드에서 처리
     */
    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    // 날짜 정규화
                    val normalizedEvent = event.copy(
                        scheduleDate = DateUtils.normalizeDate(event.scheduleDate)
                    )

                    // 날짜별 키 생성
                    val dateKey = DateUtils.getDateKey(normalizedEvent.scheduleDate)

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Adding event: ${normalizedEvent.title} for date: $dateKey")
                    }

                    // ConcurrentHashMap을 사용하여 thread-safe하게 이벤트 추가
                    eventsMap.getOrPut(dateKey) { mutableListOf() }.add(normalizedEvent)

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Total events for $dateKey: ${eventsMap[dateKey]?.size}")
                    }
                }

                // UI 업데이트
                refreshCalendarDays()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add event", e)
                _uiState.update { it.copy(error = "이벤트 추가 실패: ${e.message}") }
            }
        }
    }

    /**
     * 여러 이벤트를 한 번에 추가 (배치 처리)
     */
    fun addEvents(events: List<CalendarEvent>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    events.forEach { event ->
                        val normalizedEvent = event.copy(
                            scheduleDate = DateUtils.normalizeDate(event.scheduleDate)
                        )
                        val dateKey = DateUtils.getDateKey(normalizedEvent.scheduleDate)
                        eventsMap.getOrPut(dateKey) { mutableListOf() }.add(normalizedEvent)
                    }
                }

                refreshCalendarDays()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add events", e)
                _uiState.update { it.copy(error = "이벤트 추가 실패") }
            }
        }
    }

    /**
     * 이벤트 삭제
     */
    fun removeEvent(event: CalendarEvent) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val dateKey = DateUtils.getDateKey(event.scheduleDate)
                    eventsMap[dateKey]?.remove(event)

                    // 빈 리스트면 제거
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
     * 오래된 이벤트 정리 (메모리 관리)
     */
    fun clearOldEvents(beforeDate: Date) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val normalizedDate = DateUtils.normalizeDate(beforeDate)
                    val keysToRemove = mutableListOf<String>()

                    eventsMap.forEach { (dateKey, events) ->
                        events.removeAll { it.scheduleDate.before(normalizedDate) }
                        if (events.isEmpty()) {
                            keysToRemove.add(dateKey)
                        }
                    }

                    keysToRemove.forEach { eventsMap.remove(it) }
                }

                refreshCalendarDays()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear old events", e)
            }
        }
    }

    /**
     * 날짜 선택 처리 - ANR 방지
     */
    fun selectDay(day: CalendarDay) {
        viewModelScope.launch {
            try {
                val updatedDays = withContext(Dispatchers.Default) {
                    val currentDays = _uiState.value.calendarDays

                    currentDays.map { calendarDay ->
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

    /**
     * 달력 생성 - 백그라운드에서 처리하여 ANR 방지
     */
    private fun generateCalendarDays() {
        viewModelScope.launch {
            try {
                val days = withContext(Dispatchers.Default) {
                    calculateCalendarDays()
                }

                _uiState.update { it.copy(calendarDays = days, error = null) }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate calendar", e)
                _uiState.update { it.copy(error = "달력 생성 실패") }
            }
        }
    }

    /**
     * 백그라운드에서 실행될 달력 계산 로직
     * ANR을 방지하기 위해 복잡한 계산은 모두 Default dispatcher에서 수행
     */
    private suspend fun calculateCalendarDays(): List<CalendarDay> = withContext(Dispatchers.Default) {
        val days = mutableListOf<CalendarDay>()

        // currentCalendar 접근 시 동기화
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

        // 이전 달 날짜들 추가
        addPreviousMonthDays(days, year, month, firstDayOfWeek)

        // 현재 달 날짜들 추가
        addCurrentMonthDays(
            days = days,
            year = year,
            month = month,
            daysInMonth = daysInMonth,
            today = today,
            currentSelectedDate = currentSelectedDate
        )

        // 다음 달 날짜들 추가
        addNextMonthDays(days, year, month)

        days
    }

    /**
     * 이전 달 날짜 추가
     */
    private fun addPreviousMonthDays(
        days: MutableList<CalendarDay>,
        currentYear: Int,
        currentMonth: Int,
        firstDayOfWeek: Int
    ) {
        val prevMonth = if (currentMonth == Calendar.JANUARY) Calendar.DECEMBER else currentMonth - 1
        val prevYear = if (currentMonth == Calendar.JANUARY) currentYear - 1 else currentYear

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, prevYear)
            set(Calendar.MONTH, prevMonth)
        }
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevMonthStartDay = daysInPrevMonth - (firstDayOfWeek - Calendar.SUNDAY) + 1

        for (i in prevMonthStartDay..daysInPrevMonth) {
            days.add(
                CalendarDay(
                    day = i,
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

    /**
     * 현재 달 날짜 추가
     */
    private fun addCurrentMonthDays(
        days: MutableList<CalendarDay>,
        year: Int,
        month: Int,
        daysInMonth: Int,
        today: Calendar,
        currentSelectedDate: CalendarDay?
    ) {
        for (i in 1..daysInMonth) {
            val isToday = year == today.get(Calendar.YEAR) &&
                    month == today.get(Calendar.MONTH) &&
                    i == today.get(Calendar.DAY_OF_MONTH)

            val isSelected = currentSelectedDate?.let { selected ->
                year == selected.year &&
                        month == selected.month &&
                        i == selected.day
            } ?: false

            val eventsForDay = getEventsForDate(year, month, i)

            days.add(
                CalendarDay(
                    day = i,
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

    /**
     * 다음 달 날짜 추가
     */
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

    /**
     * 달력 새로고침
     */
    private fun refreshCalendarDays() {
        generateCalendarDays()
    }

    /**
     * 이전 달로 이동
     */
    fun goToPreviousMonth() {
        synchronized(calendarLock) {
            currentCalendar.add(Calendar.MONTH, -1)
        }
        generateCalendarDays()
    }

    /**
     * 다음 달로 이동
     */
    fun goToNextMonth() {
        synchronized(calendarLock) {
            currentCalendar.add(Calendar.MONTH, 1)
        }
        generateCalendarDays()
    }

    /**
     * 특정 달로 이동
     */
    fun goToMonth(year: Int, month: Int) {
        synchronized(calendarLock) {
            currentCalendar.set(Calendar.YEAR, year)
            currentCalendar.set(Calendar.MONTH, month)
        }
        generateCalendarDays()
    }

    /**
     * 년/월 텍스트 가져오기
     */
    fun getYearMonthText(): String {
        synchronized(calendarLock) {
            val year = currentCalendar.get(Calendar.YEAR)
            val month = currentCalendar.get(Calendar.MONTH) + 1
            return "${year}년 ${month}월"
        }
    }

    /**
     * 특정 날짜의 이벤트 조회
     */
    fun getEventsForDay(day: CalendarDay): List<CalendarEvent> {
        return getEventsForDate(day.year, day.month, day.day)
    }

    /**
     * 특정 날짜의 이벤트 조회 (내부용)
     */
    private fun getEventsForDate(year: Int, month: Int, day: Int): List<CalendarEvent> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        val dateKey = DateUtils.getDateKey(calendar.time)

        // ConcurrentHashMap이므로 별도 동기화 불필요
        return eventsMap[dateKey]?.toList() ?: emptyList()
    }

    /**
     * 사용 가능한 캘린더 목록 로드
     */
    fun loadAvailableCalendars() {
        viewModelScope.launch {
            try {
                val calendars = withContext(Dispatchers.IO) {
                    repository.getDeviceCalendars()
                }
                _uiState.update { it.copy(availableCalendars = calendars) }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load calendars", e)
                _uiState.update { it.copy(error = "캘린더 로드 실패") }
            }
        }
    }

    /**
     * 펫 목록 로드
     */
    fun loadPets() {
        viewModelScope.launch {
            try {
                val pets = withContext(Dispatchers.IO) {
                    repository.getPets()
                }
                _uiState.update { it.copy(pets = pets) }

            } catch (e: Exception) {
                Log.e(TAG, "Failed to load pets", e)
                _uiState.update { it.copy(error = "펫 로드 실패") }
            }
        }
    }

    /**
     * 더미 펫 데이터 로드
     */
    private fun loadDummyPets() {
        val dummyPets = listOf(
            Pet(id = "1", name = "코코", imageUrl = null, color = null),
            Pet(id = "2", name = "몽이", imageUrl = null, color = null),
            Pet(id = "3", name = "초코", imageUrl = null, color = null)
        )
        _uiState.update { it.copy(pets = dummyPets) }
    }

    /**
     * 펫 필터 선택
     */
    fun selectPetFilter(pet: Pet?) {
        _uiState.update { it.copy(selectedPet = pet) }
        // TODO: 펫별 이벤트 필터링 구현
        generateCalendarDays()
    }

    /**
     * 에러 메시지 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 모든 이벤트 클리어
     */
    fun clearAllEvents() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    eventsMap.clear()
                }
                refreshCalendarDays()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear events", e)
                _uiState.update { it.copy(error = "이벤트 삭제 실패") }
            }
        }
    }
}

/**
 * UI 상태를 담는 데이터 클래스
 */
data class CalendarUiState(
    val calendarDays: List<CalendarDay> = emptyList(),
    val selectedDate: CalendarDay? = null,
    val availableCalendars: List<DeviceCalendar> = emptyList(),
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)

/**
 * 달력 정보를 담는 데이터 클래스
 */
private data class CalendarInfo(
    val year: Int,
    val month: Int,
    val firstDayOfWeek: Int,
    val daysInMonth: Int
)

/**
 * 날짜 유틸리티 클래스
 */
object DateUtils {
    /**
     * 날짜 정규화 (시간을 00:00:00으로 설정)
     */
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

    /**
     * 날짜 키 생성 (YYYY-MM-DD 형식)
     */
    fun getDateKey(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return String.format(
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    /**
     * 두 날짜가 같은 날인지 확인
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
}

/**
 * 더미 데이터 클래스들
 */
data class DeviceCalendar(
    val id: String,
    val name: String,
    val accountName: String,
    val isSelected: Boolean = false
)