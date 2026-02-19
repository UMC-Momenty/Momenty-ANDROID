package com.example.momenty.domain.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.momenty.domain.calendar.CalendarRepository
import com.example.momenty.domain.calendar.CalendarViewModel
import com.example.momenty.domain.mypage.MyPageRepository
import com.example.momenty.domain.mypage.MyPageViewModel

class QuestViewModelFactory(
    private val repository: QuestRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}