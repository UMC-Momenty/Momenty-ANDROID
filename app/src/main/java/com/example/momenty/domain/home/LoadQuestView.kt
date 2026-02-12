package com.example.momenty.domain.home

interface LoadQuestView {
    fun onLoadQuestSuccess(loadQuestData: LoadQuestData)
    fun onLoadQuestFailure()
}