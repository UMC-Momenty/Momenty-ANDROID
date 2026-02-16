package com.example.momenty.domain.record.write

data class RecordItem(
    val momentId: Long,
    val createdAtRaw: String,
    val date: String,
    val title: String,
    val mood: String,
    val content: String,
    val imageRes: Int
)
