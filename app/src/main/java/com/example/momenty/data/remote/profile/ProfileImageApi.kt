package com.example.momenty.data.remote.profile

import com.example.momenty.global.api.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ProfileImageApi {

    /**
     * 프로필 이미지 업로드용 Presigned URL 발급
     * @param request 이미지 타입 (JPEG, PNG)
     * @return Presigned URL과 이미지 키
     */
    @POST("api/profile/image")
    suspend fun createProfileImagePresignedUrl(
        @Body request: ProfileImageCreateRequest
    ): BaseResponse<PresignedUrlResponse>
}

/**
 * 프로필 이미지 Presigned URL 요청
 */
data class ProfileImageCreateRequest(
    val imageType: ImageContentType
)

/**
 * Presigned URL 응답
 */
data class PresignedUrlResponse(
    val key: String,      // S3에 저장될 이미지 키 (예: "profile/uuid")
    val url: String       // Presigned Upload URL
)

/**
 * 이미지 타입
 */
enum class ImageContentType {
    JPEG,
    PNG
}