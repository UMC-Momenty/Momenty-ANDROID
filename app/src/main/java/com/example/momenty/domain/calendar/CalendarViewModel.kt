package com.example.momenty.domain.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    private var currentCalendar = Calendar.getInstance()
    private var allEvents: MutableList<CalendarEvent> = mutableListOf()

    fun initialize() {
        loadDummyPets()
        generateCalendarDays()
    }

    /**
     * 이벤트 추가 (알림에서 호출)
     */
    fun addEvent(event: CalendarEvent) {
        Log.d("CalendarViewModel", "Adding event: ${event.title} on ${event.scheduleDate}")
        allEvents.add(event)

        // 달력 다시 그리기 - 이벤트 인디케이터 업데이트
        generateCalendarDays()

        // 현재 선택된 날짜가 있다면, 해당 날짜의 이벤트 목록도 업데이트
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

        Log.d("CalendarViewModel", "Total events: ${allEvents.size}")
    }

    /**
     * 날짜 선택 처리
     */
    fun selectDay(day: CalendarDay) {
        val currentDays = _calendarDays.value ?: return

        // 모든 날짜의 isSelected를 false로 설정하고, 클릭한 날짜만 true로 설정
        val updatedDays = currentDays.map { calendarDay ->
            calendarDay.copy(
                isSelected = calendarDay.day == day.day &&
                        calendarDay.month == day.month &&
                        calendarDay.year == day.year,
                // isToday는 유지 (날짜 선택과 무관)
                isToday = calendarDay.isToday
            )
        }

        _calendarDays.value = updatedDays
        _selectedDate.value = updatedDays.find { it.isSelected }
    }

    /**
     * 달력 생성
     */
    private fun generateCalendarDays() {
        val days = mutableListOf<CalendarDay>()
        val calendar = currentCalendar.clone() as Calendar
        val today = Calendar.getInstance()

        // 현재 월의 1일로 설정
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달의 날짜들
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

        // 현재 선택된 날짜 정보 저장
        val currentSelectedDate = _selectedDate.value

        // 현재 달의 날짜들
        calendar.add(Calendar.MONTH, 1)
        for (i in 1..daysInMonth) {
            val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    i == today.get(Calendar.DAY_OF_MONTH)

            // 이전에 선택된 날짜인지 확인
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

            Log.d("CalendarViewModel", "Day $i has ${eventsForDay.size} events")

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

        // 다음 달의 날짜들
        calendar.add(Calendar.MONTH, 1)
        val remainingDays = 42 - days.size // 6주 * 7일 = 42칸
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

        _calendarDays.value = days
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
        val filtered = allEvents.filter { event ->
            val eventCal = Calendar.getInstance().apply { time = event.scheduleDate }
            val matches = eventCal.get(Calendar.YEAR) == year &&
                    eventCal.get(Calendar.MONTH) == month &&
                    eventCal.get(Calendar.DAY_OF_MONTH) == day

            if (matches) {
                Log.d("CalendarViewModel", "Event '${event.title}' matches date $year-$month-$day")
            }
            matches
        }
        return filtered
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