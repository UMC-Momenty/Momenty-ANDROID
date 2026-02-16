package com.example.momenty.domain.mypage

import com.google.gson.annotations.SerializedName

data class LoadProfileData(
    /*
    @SerializedName(value="userId") val userId: Long,
    @SerializedName(value="username") val username: String,
    @SerializedName(value="profileUrl") val profileUrl: String?,
    @SerializedName(value="pets") val pets: ArrayList<T>*/
    @SerializedName(value="username") val username: String,
    @SerializedName(value="gender") val gender: String,
    @SerializedName(value="birth") val birth: String,
    @SerializedName(value="profileUrl") val profileUrl: String,
    @SerializedName(value="questTime") val questTime: String,
    @SerializedName(value="resetQuestTime") val resetQuestTime: Boolean,
)

data class PetProfileData(
    @SerializedName(value="petId") val petId: Int = 1,
    @SerializedName(value="petName") val petName: String,
    @SerializedName(value="profileImageUrl") val profileImageUrl: String?
)

data class UpdateUserProfileRequest(
    @SerializedName(value="username") var username: String,
    @SerializedName(value="profile") var profile: String?,
    @SerializedName(value="gender") var gender: String,
    @SerializedName(value="birth") var birth: String,
    @SerializedName(value="quest_time") var quest_time: String?
)

data class UpdatePetProfileRequest(
    @SerializedName(value="pet_id") var pet_id: Int,
    @SerializedName(value="pet_name") var pet_name: String,
    @SerializedName(value="profile") var profile: String?,
    @SerializedName(value="gender") var gender: String,
    @SerializedName(value="birth") var birth: String,
    @SerializedName(value="species") var species: String,
    @SerializedName(value="breed_name") var breed_name: String?,
    @SerializedName(value="intro") var intro: String?
)

data class AddPetProfileRequest(
    @SerializedName(value="pet_id") var pet_id: Int,
    @SerializedName(value="pet_name") var pet_name: String,
    @SerializedName(value="profile") var profile: String?,
    @SerializedName(value="gender") var gender: String,
    @SerializedName(value="birth") var birth: String,
    @SerializedName(value="species") var species: String,
    @SerializedName(value="breed_name") var breed_name: String?,
    @SerializedName(value="intro") var intro: String?
)

data class LoadNoticeData<T1, T2>(
    @SerializedName(value = "notices") var notices: ArrayList<T1>,
    @SerializedName(value = "pageInfo") var pageInfo: T2
)
data class _NoticeData(
    @SerializedName(value="noticeId") var noticeId: Int,
    @SerializedName(value="title") var title: String,
    @SerializedName(value="createdAt") var createdAt: String
)
data class _PageInfoData(
    @SerializedName(value="page") var page: Int,
    @SerializedName(value="size") var size: Int,
    @SerializedName(value="totalPages") var totalPages: Int,
    @SerializedName(value="totalElements") var totalElements: Int,
    @SerializedName(value="hasNext") var hasNext: Boolean,
    @SerializedName(value="hasPrevious") var hasPrevious: Boolean,
)

data class LoadNoticeDetailData(
    @SerializedName(value="noticeId") var noticeId: Int,
    @SerializedName(value="title") var title: String,
    @SerializedName(value="content") var content: String,
    @SerializedName(value="createdAt") var createdAt: String
)

data class AddInquiryRequest<T>(
    @SerializedName(value="type") var type: String,
    @SerializedName(value="content") var content: String,
    @SerializedName(value="images") var images: ArrayList<T>
)
data class AddInquiryRequestImg(
    @SerializedName(value = "imageKey") var imageKey: String?
)

data class GetImageUrlRequest(
    @SerializedName(value = "imageTypes") var imageTypes: ArrayList<String>
)
data class GetImageUrlData(
    @SerializedName(value = "key") var key: String,
    @SerializedName(value = "url") var url: String
)

data class LoadInquiryData<T1, T2>(
    @SerializedName(value = "inquiries") var inquiries: ArrayList<T1>,
    @SerializedName(value = "pageInfo") var pageInfo: T2,
)
data class LoadInquiryDataInquiries(
    @SerializedName(value="inquiryId") var inquiryId: Int,
    @SerializedName(value="type") var type: String,
    @SerializedName(value="isAnswered") var isAnswered: Boolean,
    @SerializedName(value="createdAt") var createdAt: String
)
data class LoadInquiryDataPageInfo(
    @SerializedName(value="page") var page: Int,
    @SerializedName(value="size") var size: Int,
    @SerializedName(value="totalPages") var totalPages: Int,
    @SerializedName(value="totalElements") var totalElements: Int,
    @SerializedName(value="hasNext") var hasNext: Boolean,
    @SerializedName(value="hasPrevious") var hasPrevious: Boolean,
)

data class LoadInquiryDetailData<T>(
    @SerializedName(value="type") var type: String,
    @SerializedName(value="content") var content: String,
    @SerializedName(value="images") var images: ArrayList<T>
)
data class LoadInquiryDetailDataImages(
    @SerializedName(value="imageKey") var imageKey: String
)

data class LoadFaqData(
    @SerializedName(value="faqId") var faqId: Int,
    @SerializedName(value="question") var question: String
)

data class LoadFaqDetailData(
    @SerializedName(value="faqId") var faqId: Int,
    @SerializedName(value="question") var question: String,
    @SerializedName(value="answer") var answer: String
)