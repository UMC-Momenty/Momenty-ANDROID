package com.example.momenty.domain.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class QuestViewModel(private val repository: QuestRepository): ViewModel() {
    private val _writeQuestResult = MutableLiveData<Result<WriteQuestData>>()
    val writeQuestResult: LiveData<Result<WriteQuestData>> = _writeQuestResult

    private val _loadQuestResult = MutableLiveData<Result<LoadQuestData>>()
    val loadQuestResult: LiveData<Result<LoadQuestData>> = _loadQuestResult

    fun writeQuest(accessToken:String, questId: String, petId: String, answer: String) {
        viewModelScope.launch {
            val request = WriteQuestRequest(questId, petId, answer)
            val result = repository.writeQuest(accessToken, request)
            _writeQuestResult.postValue(result)
        }
    }

    fun loadQuest(accessToken:String) {
        viewModelScope.launch {
            val result = repository.loadQuest(accessToken)
            _loadQuestResult.postValue(result)
        }
    }
}