package com.example.momenty.domain.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CalendarViewModelFactory(
    private val repository: CalendarRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}