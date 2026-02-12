package com.example.momenty.domain.mypage

data class CustomerCenterInquiryHistoryData(
    val title: String = "",
    val date: String = "",
    val inquiryType: String = "",
    val inquiryContent: String = "",
    val photo1: String ?= null,
    val photo2: String ?= null,
    val answer: String = ""
)
