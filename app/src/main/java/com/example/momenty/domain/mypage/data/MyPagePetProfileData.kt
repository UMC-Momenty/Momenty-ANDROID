package com.example.momenty.domain.mypage.data

data class MyPagePetProfileData(
    val name: String = "",
    val gender: String = "",
    val birth: String = "",
    val type: String = "",
    val typeDetail: String ?= "",
    val intro: String ?= "",
    val imageKey: String ?= null,
    val imageUri: String ?= null
)