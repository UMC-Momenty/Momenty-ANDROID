package com.example.momenty.domain.mypage

import com.example.momenty.domain.home.LoadQuestData
import com.example.momenty.domain.home.LoadQuestResponse
import com.example.momenty.domain.home.WriteQuestData
import com.example.momenty.domain.home.WriteQuestRequest
import com.example.momenty.domain.home.WriteQuestResponse
import com.example.momenty.global.api.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface MyPageService {
    @GET("api/mypage/{userId}")
    suspend fun loadProfile(
        @Header("Authorization") token: String,
        @Path("userId") id: Long
    ): Response<BaseResponse<LoadProfileData<PetProfileData>>>

    @PATCH("api/mypage/{userId}")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Path("userId") id: Long,
        @Body req: UpdateUserProfileRequest
    ): Response<BaseResponse<Unit>>

    @PATCH("api/users/{userId}/pets/{petId}")
    suspend fun updatePetProfile(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Path("petId") petId: Int,
        @Body req: UpdatePetProfileRequest
    ): Response<BaseResponse<Unit>>

    @POST("api/users/{userId}/pets")
    suspend fun addPetProfile(
        @Header("Authorization") token: String,
        @Path("userId") id: Long,
        @Body req: AddPetProfileRequest
    ): Response<BaseResponse<Unit>>

    @GET("api/notice")
    suspend fun loadNotice(
        @Header("Authorization") token: String
    ): Response<BaseResponse<LoadNoticeData<_NoticeData, _PageInfoData>>>

    @GET("api/notice/{noticeId}")
    suspend fun loadNoticeDetail(
        @Header("Authorization") token: String,
        @Path("noticeId") id: Int
    ): Response<BaseResponse<LoadNoticeDetailData>>

    @POST("api/inquiry/{userId}")
    suspend fun addInquiry(
        @Header("Authorization") token: String,
        @Path("userId") id: Long,
        @Body req: AddInquiryRequest<AddInquiryRequestImg>
    ): Response<BaseResponse<String>>

    @POST("api/inquiry/image")
    suspend fun getImageUrl(
        @Header("Authorization") token: String,
        @Body req: GetImageUrlRequest
    ): Response<BaseResponse<ArrayList<GetImageUrlData>>>

    @GET("api/inquiry/user/{userId}")
    suspend fun loadInquiry(
        @Header("Authorization") token: String,
        @Path("userId") id: Long
    ): Response<BaseResponse<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>>>

    @GET("api/inquiry/{inquiryId}")
    suspend fun loadInquiryDetail(
        @Header("Authorization") token: String,
        @Path("inquiryId") id: Int
    ): Response<BaseResponse<LoadInquiryDetailData<LoadInquiryDetailDataImages>>>

    @GET("api/faq")
    suspend fun loadFaq(
        @Header("Authorization") token: String
    ): Response<BaseResponse<ArrayList<LoadFaqData>>>

    @GET("api/faq/{faqId}")
    suspend fun loadFaqDetail(
        @Header("Authorization") token: String,
        @Path("faqId") id: Int
    ): Response<BaseResponse<LoadFaqDetailData>>
}
