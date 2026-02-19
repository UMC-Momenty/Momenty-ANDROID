package com.example.momenty.domain.mypage

import com.example.momenty.global.api.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MyPageService {
    @PATCH("khg/api/mypage") // TODO: 실제 주소 수정 바람!!!!!
    suspend fun updateUserProfile( // DONE
        @Body req: UpdateUserProfileRequest
    ): Response<BaseResponse<UpdateUserProfileData>>

    @GET("khg/api/mypage/{userId}") // TODO: 실제 주소 수정 바람!!!!!
    suspend fun loadProfile( // DONE
        @Path("userId") id: Long
    ): Response<BaseResponse<LoadProfileData>>

    @GET("/api/pets/{petId}")
    suspend fun loadOnePetProfile( // DONE
        @Path("petId") petId: Long
    ): Response<BaseResponse<LoadOnePetProfileData>>

    @POST("api/users/{userId}/pets")
    suspend fun addPetProfile( // DONE
        @Path("userId") id: Long,
        @Body req: AddPetProfileRequest
    ): Response<BaseResponse<String?>>

    @PATCH("api/users/{userId}/pets/{petId}")
    suspend fun updatePetProfile( // DONE
        @Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Body req: UpdatePetProfileRequest
    ): Response<BaseResponse<String?>>

    @GET("api/notice")
    suspend fun loadNotice(
        @Header("Authorization") token: String
    ): Response<BaseResponse<LoadNoticeData<_NoticeData, _PageInfoData>>>

    @GET("api/notice/{noticeId}")
    suspend fun loadNoticeDetail(
        @Header("Authorization") token: String,
        @Path("noticeId") id: Long
    ): Response<BaseResponse<LoadNoticeDetailData>>

    @POST("api/inquiry") // DONE
    suspend fun addInquiry(
        @Body req: AddInquiryRequest<AddInquiryRequestImg>
    ): Response<BaseResponse<String?>>

    @POST("api/inquiry/image") // DONE
    suspend fun createInquiryPresignedUrls(
        @Body req: GetImageUrlRequest
    ): Response<BaseResponse<ArrayList<GetImageUrlData>>>

    @GET("api/inquiry/user")
    suspend fun loadInquiry(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: ArrayList<String>
    ): Response<BaseResponse<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>>>


    @GET("api/inquiry/{inquiryId}")
    suspend fun loadInquiryDetail(
        @Path("inquiryId") id: Long
    ): Response<BaseResponse<LoadInquiryDetailData<LoadInquiryDetailDataImages>>>

    @GET("api/faq")
    suspend fun loadFaq(
        @Header("Authorization") token: String
    ): Response<BaseResponse<ArrayList<LoadFaqData>>>

    @GET("api/faq/{faqId}")
    suspend fun loadFaqDetail(
        @Header("Authorization") token: String,
        @Path("faqId") id: Long
    ): Response<BaseResponse<LoadFaqDetailData>>

    @POST("api/oauth/logout")
    suspend fun logout(): Response<BaseResponse<String?>>
}
