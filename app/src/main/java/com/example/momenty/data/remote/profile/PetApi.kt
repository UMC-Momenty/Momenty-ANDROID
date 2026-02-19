package com.example.momenty.data.remote.profile

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PetApi {

    /**
     * 내 반려동물 목록 조회 - GET /api/pets/my
     * userId 없이 토큰으로 처리
     */
    @GET("api/pets/my")
    suspend fun getMyPets(): BaseResponse<List<PetSummaryResponse>>

    /**
     * 특정 반려동물 프로필 조회 - GET /api/pets/{petId}
     */
    @GET("api/pets/{petId}")
    suspend fun getPetProfile(
        @Path("petId") petId: Long
    ): BaseResponse<PetProfileResponse>

    /**
     * 반려동물 프로필 추가 - POST /api/pets
     * userId 없이 토큰으로 처리
     */
    @POST("api/pets")
    suspend fun addPet(
        @Body request: AddPetRequest
    ): BaseResponse<String>

    /**
     * 반려동물 프로필 수정 - PATCH /api/pets/{petId}
     * userId 없이 토큰으로 처리
     */
    @PATCH("api/pets/{petId}")
    suspend fun updatePetProfile(
        @Path("petId") petId: Long,
        @Body request: UpdatePetProfileRequest
    ): BaseResponse<String>
}

/**
 * 내 반려동물 목록 응답
 */
data class PetSummaryResponse(
    val petId: Long,
    val petName: String,
    val species: String,        // "CAT" or "DOG"
    val breedName: String?,
    val profileImageUrl: String?
)

/**
 * 특정 반려동물 프로필 조회 응답
 */
data class PetProfileResponse(
    val petId: Long,
    val profileImageUrl: String?,
    val petName: String,
    val gender: String,         // "MALE" or "FEMALE"
    val birth: String,          // "2026-02-19"
    val species: String,        // "CAT" or "DOG"
    val breedName: String?,
    val intro: String?
)

/**
 * 반려동물 추가 요청 (POST /api/pets)
 */
data class AddPetRequest(
    val profileImageUrl: String? = null,
    val petName: String,
    val gender: String,         // "MALE" or "FEMALE"
    val birth: String,          // "2000-01-01"
    val species: String,        // "CAT" or "DOG"
    val breedId: Long? = null,
    val intro: String? = null
)

/**
 * 반려동물 프로필 수정 요청 (PATCH /api/pets/{petId})
 */
data class UpdatePetProfileRequest(
    val profileImageUrl: String? = null,
    val petName: String? = null,
    val gender: String? = null,
    val birth: String? = null,
    val species: String? = null,
    val breedId: Long? = null,
    val intro: String? = null
)