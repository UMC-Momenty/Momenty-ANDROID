package com.example.momenty.domain.calendar.api.response

/**
 * 유저 반려동물 목록 조회 응답
 */

data class PetScheduleDto(
    val petId: Long,
    val petName: String,
    val species: String,
    val breedName: String?,
    val profileImageUrl: String?
)