package com.example.momenty.domain.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarViewModel(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _selectedDate = MutableLiveData<CalendarDay?>()
    val selectedDate: LiveData<CalendarDay?> = _selectedDate

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _availableCalendars = MutableLiveData<List<DeviceCalendar>>()
    val availableCalendars: LiveData<List<DeviceCalendar>> = _availableCalendars

    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    private val _selectedPet = MutableLiveData<Pet?>()
    val selectedPet: LiveData<Pet?> = _selectedPet

    private val eventsLock = Any() // 이벤트 동기화 위함
    private var currentCalendar = Calendar.getInstance()
    private var allEvents: MutableList<CalendarEvent> = mutableListOf()

    fun initialize() {
        loadDummyPets()
        generateCalendarDays()
    }

    /**
     * 이벤트 추가 - 백그라운드에서
     */
    fun addEvent(event: CalendarEvent) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                synchronized(eventsLock) {
                    Log.d("CalendarViewModel", "Adding event: ${event.title} on ${event.scheduleDate}")
                    allEvents.add(event)
                    Log.d("CalendarViewModel", "Total events: ${allEvents.size}")
                }
            }

            // UI 업데이트는 메인 스레드에서
            withContext(Dispatchers.Main) {
                refreshCalendarDays()
            }
        }
    }

    /**
     * 날짜 선택 처리
     */
    fun selectDay(day: CalendarDay) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentDays = _calendarDays.value ?: return@launch

            val updatedDays = currentDays.map { calendarDay ->
                calendarDay.copy(
                    isSelected = calendarDay.day == day.day &&
                            calendarDay.month == day.month &&
                            calendarDay.year == day.year,
                    isToday = calendarDay.isToday
                )
            }

            withContext(Dispatchers.Main) {
                _calendarDays.value = updatedDays
                _selectedDate.value = updatedDays.find { it.isSelected }
            }
        }
    }

    /**
     * 달력 생성 - 백그라운드에서
     */
    private fun generateCalendarDays() {
        viewModelScope.launch(Dispatchers.Default) {
            val days = calculateCalendarDays()

            withContext(Dispatchers.Main) {
                _calendarDays.value = days
            }
        }
    }

    /**
     *  백그라운드에서 실행될 달력 계산 로직
     */
    private suspend fun calculateCalendarDays(): List<CalendarDay> = withContext(Dispatchers.Default) {
        val days = mutableListOf<CalendarDay>()
        val calendar = currentCalendar.clone() as Calendar
        val today = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달
        calendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevMonthStartDay = daysInPrevMonth - (firstDayOfMonth - Calendar.SUNDAY) + 1

        for (i in prevMonthStartDay..daysInPrevMonth) {
            days.add(
                CalendarDay(
                    day = i,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR),
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false
                )
            )
        }

        val currentSelectedDate = _selectedDate.value

        // 현재 달
        calendar.add(Calendar.MONTH, 1)
        for (i in 1..daysInMonth) {
            val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    i == today.get(Calendar.DAY_OF_MONTH)

            val isSelected = currentSelectedDate?.let { selected ->
                calendar.get(Calendar.YEAR) == selected.year &&
                        calendar.get(Calendar.MONTH) == selected.month &&
                        i == selected.day
            } ?: false

            val eventsForDay = getEventsForDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                i
            )

            days.add(
                CalendarDay(
                    day = i,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR),
                    isCurrentMonth = true,
                    isToday = isToday,
                    isSelected = isSelected,
                    events = eventsForDay
                )
            )
        }

        // 다음 달
        calendar.add(Calendar.MONTH, 1)
        val remainingDays = 42 - days.size
        for (i in 1..remainingDays) {
            days.add(
                CalendarDay(
                    day = i,
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR),
                    isCurrentMonth = false,
                    isToday = false,
                    isSelected = false
                )
            )
        }

        days
    }

    /**
     *  FIX: 달력 새로고침
     */
    private fun refreshCalendarDays() {
        generateCalendarDays()

        _selectedDate.value?.let { selectedDay ->
            val updatedDay = _calendarDays.value?.find { day ->
                day.day == selectedDay.day &&
                        day.month == selectedDay.month &&
                        day.year == selectedDay.year
            }
            updatedDay?.let {
                _selectedDate.value = it
            }
        }
    }

    /**
     * 이전 달로 이동
     */
    fun goToPreviousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        generateCalendarDays()
    }

    /**
     * 다음 달로 이동
     */
    fun goToNextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        generateCalendarDays()
    }

    /**
     * 년/월 텍스트 가져오기
     */
    fun getYearMonthText(): String {
        val year = currentCalendar.get(Calendar.YEAR)
        val month = currentCalendar.get(Calendar.MONTH) + 1
        return "${year}년 ${month}월"
    }

    /**
     * 특정 날짜의 이벤트 조회
     */
    fun getEventsForDay(day: CalendarDay): List<CalendarEvent> {
        return getEventsForDate(day.year, day.month, day.day)
    }

    private fun getEventsForDate(year: Int, month: Int, day: Int): List<CalendarEvent> {
        synchronized(eventsLock) {
            return allEvents.filter { event ->
                val eventCal = Calendar.getInstance().apply { time = event.scheduleDate }
                eventCal.get(Calendar.YEAR) == year &&
                        eventCal.get(Calendar.MONTH) == month &&
                        eventCal.get(Calendar.DAY_OF_MONTH) == day
            }
        }
    }

    /**
     * 달력 로드
     */
    fun loadCalendar() {
        // TODO: 실제 API 호출
        generateCalendarDays()
    }

    /**
     * 사용 가능한 캘린더 목록 로드
     */
    fun loadAvailableCalendars() {
        viewModelScope.launch {
            try {
                // TODO: DeviceCalendarHelper에서 로드
                // val calendars = repository.getDeviceCalendars()
                // _availableCalendars.postValue(calendars)
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Failed to load calendars", e)
                _error.postValue("캘린더 로드 실패")
            }
        }
    }

    /**
     * 펫 목록 로드
     */
    fun loadPets() {
        viewModelScope.launch {
            try {
                // TODO: 실제 API 호출
                // val pets = repository.getPets()
                // _pets.postValue(pets)
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Failed to load pets", e)
                _error.postValue("펫 로드 실패")
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
        _pets.value = dummyPets
    }

    /**
     * 펫 필터 선택
     */
    fun selectPetFilter(pet: Pet?) {
        _selectedPet.value = pet
        // TODO: 필터링된 이벤트 다시 로드
        generateCalendarDays()
    }
}

// 더미 데이터 클래스들
data class DeviceCalendar(
    val id: String,
    val name: String,
    val accountName: String,
    val isSelected: Boolean = false
)