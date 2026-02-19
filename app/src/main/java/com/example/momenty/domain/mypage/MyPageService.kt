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

    // ===================== pet-controller =====================

    /**
     * 반려동물 프로필 추가
     */
    @POST("api/pets")
    suspend fun addPetProfile(
        @Body req: AddPetProfileRequest
    ): Response<BaseResponse<String?>>

    /**
     * 특정 반려동물 프로필 조회
     */
    @GET("api/pets/{petId}")
    suspend fun loadOnePetProfile(
        @Path("petId") petId: Long
    ): Response<BaseResponse<LoadOnePetProfileData>>

    /**
     * 반려동물 프로필 수정
     */
    @PATCH("api/pets/{petId}")
    //@PATCH("api/users/{userId}/pets/{petId}")
    suspend fun updatePetProfile(
        //@Path("userId") userId: Long,
        @Path("petId") petId: Long,
        @Body req: UpdatePetProfileRequest
    ): Response<BaseResponse<String?>>

    /**
     * 내 반려동물 리스트 조회
     */
    @GET("api/pets/my")
    suspend fun loadPetList(): Response<BaseResponse<ArrayList<LoadPetListData>>>



    // ===================== inquiry-controller =====================

    /**
     * 문의하기
     */
    @POST("api/inquiry")
    suspend fun addInquiry(
        @Body req: AddInquiryRequest<AddInquiryRequestImg>
    ): Response<BaseResponse<String?>>

    /**
     * 문의하기 이미지 업로드용 Presigned URL 발급
     */
    @POST("api/inquiry/image")
    suspend fun createInquiryPresignedUrls(
        @Body req: GetImageUrlRequest
    ): Response<BaseResponse<ArrayList<GetImageUrlData>>>

    /**
     * 문의내역 상세 조회
     */
    @GET("api/inquiry/{inquiryId}")
    suspend fun loadInquiryDetail(
        @Path("inquiryId") id: Long
    ): Response<BaseResponse<LoadInquiryDetailData<LoadInquiryDetailDataImages>>>

    /**
     * 문의내역 리스트 조회
     */
    @GET("api/inquiry/user")
    suspend fun loadInquiry(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: ArrayList<String>
    ): Response<BaseResponse<LoadInquiryData<LoadInquiryDataInquiries, LoadInquiryDataPageInfo>>>



    // ===================== user-controller =====================

    /**
     * 사용자 프로필 조회
     */
    @GET("api/mypage")
    //@GET("api/mypage/{userId}") // TODO: 실제 주소 수정 바람!!!!!
    suspend fun loadProfile(
        //@Path("userId") id: Long
    ): Response<BaseResponse<LoadProfileData>>

    /**
     * 사용자 프로필 수정
     */
    @PATCH("api/mypage")
    suspend fun updateUserProfile(
        @Body req: UpdateUserProfileRequest
    ): Response<BaseResponse<UpdateUserProfileData>>

    /**
     * 사용자 상세 조회
     */
    @GET("api/mypage/user")
    suspend fun loadProfileDetail(): Response<BaseResponse<LoadProfileDetailData>>



    // ===================== notice-controller =====================

    /**
     * 공지사항 리스트 조회
     */
    @GET("api/notice")
    suspend fun loadNotice(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sort") sort: ArrayList<String>
    ): Response<BaseResponse<LoadNoticeData<_NoticeDatas, _PageInfoData>>>

    /**
     * 공지사항 상세 조회
     */
    @GET("api/notice/{noticeId}")
    suspend fun loadNoticeDetail(
        @Path("noticeId") id: Long
    ): Response<BaseResponse<LoadNoticeDetailData>>



    // ===================== faq-controller =====================

    /**
     * FAQ 리스트 조회
     */
    @GET("api/faq")
    suspend fun loadFaq(): Response<BaseResponse<ArrayList<LoadFaqData>>>

    /**
     * FAQ 상세 조회
     */
    @GET("api/faq/{faqId}")
    suspend fun loadFaqDetail(
        @Path("faqId") id: Long
    ): Response<BaseResponse<LoadFaqDetailData>>



    // ===================== UNKNOWN =====================

    @POST("api/oauth/logout")
    suspend fun logout(): Response<BaseResponse<String?>>


    /*
    @GET("api/users/{userId}/schedules/pets")
    suspend fun loadPetsProfile(
        @Path("userId") userId: Long
    ): Response<BaseResponse<ArrayList<LoadOnePetProfileData>>>*/
}

data class LoadUserDetailData(
    val userId: Long,
    val profileUrl: String?,
    val username: String,
    val gender: String,
    val birth: String,
    val questTime: String?
)