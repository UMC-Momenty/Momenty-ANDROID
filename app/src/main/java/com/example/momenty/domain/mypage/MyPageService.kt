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
    @GET("api/mypage")
    suspend fun loadProfile(): Response<BaseResponse<LoadProfileData>>

    @PATCH("api/mypage")
    suspend fun updateUserProfile(
        @Body req: UpdateUserProfileRequest
    ): Response<BaseResponse<UpdateUserProfileData>>

    @GET("api/mypage/user")
    suspend fun loadUserDetail(): Response<BaseResponse<LoadUserDetailData>>

    @GET("/api/pets/{petId}")
    suspend fun loadOnePetProfile( // DONE
        @Path("petId") petId: Long
    ): Response<BaseResponse<LoadOnePetProfileData>>

    @POST("api/pets")
    suspend fun addPetProfile( // DONE
        @Body req: AddPetProfileRequest
    ): Response<BaseResponse<String?>>

    @PATCH("api/pets/{petId}")
    suspend fun updatePetProfile( // DONE
        @Path("petId") petId: Long,
        @Body req: UpdatePetProfileRequest
    ): Response<BaseResponse<String?>>

    @GET("api/notice")
    suspend fun loadNotice(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "createdAt,DESC"
    ): Response<BaseResponse<LoadNoticeData<_NoticeData, _PageInfoData>>>

    @GET("api/notice/{noticeId}")
    suspend fun loadNoticeDetail(
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
    suspend fun loadFaq(): Response<BaseResponse<ArrayList<LoadFaqData>>>

    @GET("api/faq/{faqId}")
    suspend fun loadFaqDetail(
        @Path("faqId") id: Long
    ): Response<BaseResponse<LoadFaqDetailData>>

    @POST("api/oauth/logout")
    suspend fun logout(): Response<BaseResponse<String?>>
}

data class LoadUserDetailData(
    val userId: Long,
    val profileUrl: String?,
    val username: String,
    val gender: String,
    val birth: String,
    val questTime: String?
)