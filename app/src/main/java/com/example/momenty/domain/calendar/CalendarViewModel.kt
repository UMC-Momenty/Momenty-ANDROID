package com.example.momenty.domain.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _selectedDate = MutableLiveData<CalendarDay?>()
    val selectedDate: LiveData<CalendarDay?> = _selectedDate

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _availableCalendars = MutableLiveData<List<DeviceCalendarHelper.DeviceCalendar>>()
    val availableCalendars: LiveData<List<DeviceCalendarHelper.DeviceCalendar>> = _availableCalendars

    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    private val _selectedPet = MutableLiveData<Pet?>(null) // null = 전체 일정
    val selectedPet: LiveData<Pet?> = _selectedPet

    private var currentMonth = Calendar.getInstance()
    private var allEvents = listOf<CalendarEvent>()

    /**
     * 캘린더 로드
     */
    fun loadCalendar()  {
        viewModelScope.launch {
            _isLoading.value = true
            try{
                val startDate = getMonthStartDate()
                val endDate = getMonthEndDate()

                val result = repository.getEventsInRange(
                    startDate = startDate.timeInMillis,
                    endDate = endDate.timeInMillis,
                    selectedPetId = _selectedPet.value?.id
                )

                result.onSuccess { events ->
                    allEvents = events
                    generateCalendarDays(events)
                }.onFailure { exception ->
                    _error.value = exception.message ?: "일정을 불러오는데 실패했습니다"
                }
            }catch (e: Exception){
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            }finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 사용 가능한 캘린더 목록 로드
     */
    fun loadAvailableCalendars() {
        viewModelScope.launch {
            val result = repository.getAvailableCalendars()
            result.onSuccess { calendars ->
                _availableCalendars.value = calendars
            }.onFailure { exception ->
                _error.value = exception.message ?: "캘린더 목록을 불러오는데 실패했습니다"
            }
        }
    }

    /**
     * 반려동물 목록 로드
     */
    fun loadPets() {
        viewModelScope.launch {
            val result = repository.getPets()
            result.onSuccess { pets ->
                _pets.value = pets
            }.onFailure { exception ->
                _error.value = exception.message ?: "반려동물 목록을 불러오는데 실패했습니다"
            }
        }
    }

    /**
     * 반려동물 필터 선택
     */
    fun selectPetFilter(pet: Pet?) {
        _selectedPet.value = pet
        loadCalendar() // 필터링된 일정 다시 로드
    }

    /**
     * 날짜 선택
     */
    fun selectDay(day: CalendarDay) {
        _selectedDate.value = day
        generateCalendarDays(allEvents, day)
    }

    /**
     * 이전 달로 이동
     */
    fun goToPreviousMonth() {
        currentMonth.add(Calendar.MONTH, -1)
        loadCalendar()
    }

    /**
     * 다음 달로 이동
     */
    fun goToNextMonth() {
        currentMonth.add(Calendar.MONTH, 1)
        loadCalendar()
    }

    /**
     * 년월 텍스트 가져오기
     */
    fun getYearMonthText(): String {
        val year = currentMonth.get(Calendar.YEAR)
        val month = currentMonth.get(Calendar.MONTH) + 1
        return "${year}년 ${month}월"
    }

    /**
     * 특정 날짜의 일정 가져오기
     */
    fun getEventsForDay(day: CalendarDay): List<CalendarEvent> {
        return allEvents.filter { event ->
            val eventCalendar = Calendar.getInstance().apply {
                time = event.startTime
            }
            day.isSameDay(eventCalendar)
        }
    }

    /**
     * 캘린더 날짜 생성
     */
    private fun generateCalendarDays(
        events: List<CalendarEvent>,
        selectedDay: CalendarDay? = null
    ) {
        val days = mutableListOf<CalendarDay>()
        val today = Calendar.getInstance()

        val calendar = currentMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달의 날짜들
        val prevMonthDays = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
        if (prevMonthDays > 0) {
            val prevMonthCalendar = calendar.clone() as Calendar
            prevMonthCalendar.add(Calendar.MONTH, -1)
            val prevMonthLastDay = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (i in prevMonthLastDay - prevMonthDays + 1..prevMonthLastDay) {
                days.add(
                    CalendarDay(
                        day = i,
                        month = prevMonthCalendar.get(Calendar.MONTH),
                        year = prevMonthCalendar.get(Calendar.YEAR),
                        isCurrentMonth = false
                    )
                )
            }
        }

        // 현재 달의 날짜들
        for (day in 1..daysInMonth) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, day)

            val isToday = today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == day

            val isSelected = selectedDay?.let {
                it.day == day && it.month == calendar.get(Calendar.MONTH) &&
                        it.year == calendar.get(Calendar.YEAR)
            } ?: false

            val dayEvents = events.filter { event ->
                val eventCalendar = Calendar.getInstance().apply {
                    time = event.startTime
                }
                eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                        eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                        eventCalendar.get(Calendar.DAY_OF_MONTH) == day
            }

            days.add(
                CalendarDay(
                    day = day,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR),
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected,
                    events = dayEvents
                )
            )
        }

        // 다음 달의 날짜들
        val remainingDays = 42 - days.size // 6주 * 7일
        if (remainingDays > 0) {
            val nextMonthCalendar = calendar.clone() as Calendar
            nextMonthCalendar.add(Calendar.MONTH, 1)

            for (i in 1..remainingDays) {
                days.add(
                    CalendarDay(
                        day = i,
                        month = nextMonthCalendar.get(Calendar.MONTH),
                        year = nextMonthCalendar.get(Calendar.YEAR),
                        isCurrentMonth = false
                    )
                )
            }
        }

        _calendarDays.value = days
    }

    /**
    * 월의 시작 날짜
    */
    private fun getMonthStartDate(): Calendar {
        return (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * 월의 마지막 날짜
     */
    private fun getMonthEndDate(): Calendar {
        return (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
}