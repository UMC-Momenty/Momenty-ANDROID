package com.example.momenty.domain.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QuestViewModel(private val repository: QuestRepository): ViewModel() {
    private val _writeQuestResult = MutableLiveData<Result<String>>()
    val writeQuestResult: LiveData<Result<String>> = _writeQuestResult

    private val _loadQuestResult = MutableLiveData<Result<LoadQuestData>>()
    val loadQuestResult: LiveData<Result<LoadQuestData>> = _loadQuestResult

    fun writeQuest(questId: Long, petId: Long, answer: String) {
        viewModelScope.launch {
            val request = WriteQuestRequest(questId, petId, answer)
            val result = repository.writeQuest(request)
            _writeQuestResult.postValue(result)
        }
    }

    fun loadQuest() {
        viewModelScope.launch {
            val result = repository.loadQuest()
            _loadQuestResult.postValue(result)
        }
    }
}