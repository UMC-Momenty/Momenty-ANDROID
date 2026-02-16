package com.example.momenty.data.remote.moment

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MomentsApi {

    // 게시글 조희
    @GET("/api/moments/users/{userId}/pets/{petId}")
    suspend fun getMomentsByPet(
        @Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: List<String> = listOf("createdAt,DESC"),
    ): BaseResponse<MomentListResultDto>

    // presigned 발급
    @POST("/api/moments/image")
    suspend fun createMomentImagePresignedUrl(
        @Body body: PresignedRequestDto
    ): BaseResponse<List<PresignedUrlDto>>

    // 모먼트 생성
    @POST("/api/moments/users/{userId}/pets/{petId}")
    suspend fun createMoment(
        @Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Body body: CreateMomentRequestDto
    ): BaseResponse<String>


    @GET("/api/moments/users/{userId}/pets/{petId}/{momentId}")
    suspend fun getMomentDetail(
        @Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Path("momentId") momentId: Long
    ): BaseResponse<MomentDetailResultDto>


}
