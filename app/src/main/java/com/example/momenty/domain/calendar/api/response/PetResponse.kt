package com.example.momenty.domain.calendar.api.response

/**
 * 유저 반려동물 목록 조회 응답
 */
data class UserPetsResponse(
    val pets: List<PetScheduleDto>
)

data class PetScheduleDto(
    val petId: Long,
    val profile: String
)