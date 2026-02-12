package com.example.momenty.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * 이미지 업로드 API 인터페이스
 */
interface ImageUploadApiService {

    /**
     * 프로필 이미지 업로드
     * @param image 업로드할 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Multipart
    @POST("/api/images/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>
}

/**
 * 이미지 업로드 응답 DTO
 */
data class ImageUploadResponse(
    val isSuccess: Boolean,
    val code: String,
    val result: ImageUrlResult
)

data class ImageUrlResult(
    val imageUrl: String
)