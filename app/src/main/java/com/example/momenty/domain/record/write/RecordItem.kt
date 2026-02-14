package com.example.momenty.domain.record.write

data class RecordItem(
    val date: String,
    val title: String,
    val mood: String,
    val content: String,
    val imageRes: Int
)

