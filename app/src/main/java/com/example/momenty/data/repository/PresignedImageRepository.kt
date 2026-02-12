package com.example.momenty.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.momenty.data.remote.profile.ImageContentType
import com.example.momenty.data.remote.profile.ProfileImageApi
import com.example.momenty.data.remote.profile.ProfileImageCreateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Presigned URL 방식의 이미지 업로드 Repository
 */
@Singleton
class PresignedImageRepository @Inject constructor(
    private val profileImageApi: ProfileImageApi,
    private val okHttpClient: OkHttpClient
) {

    /**
     * 이미지를 Presigned URL 방식으로 업로드
     *
     * 1. 백엔드에서 Presigned URL 발급
     * 2. S3에 직접 업로드
     * 3. 이미지 키(key) 반환
     *
     * @param context Context
     * @param imageUri 업로드할 이미지 URI
     * @return 업로드된 이미지 키(key)
     */
    suspend fun uploadImageWithPresignedUrl(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. 이미지 타입 결정
            val imageType = getImageType(context, imageUri)

            // 2. Presigned URL 요청
            val presignedResponse = profileImageApi.createProfileImagePresignedUrl(
                ProfileImageCreateRequest(imageType = imageType)
            )

            if (!presignedResponse.isSuccess || presignedResponse.result == null) {
                return@withContext Result.failure(
                    Exception("Presigned URL 발급 실패: ${presignedResponse.message}")
                )
            }

            val presignedData = presignedResponse.result!!
            val uploadUrl = presignedData.url
            val imageKey = presignedData.key

            Log.d(TAG, "Presigned URL 발급 성공: key=$imageKey")

            // 3. S3에 직접 업로드
            val uploadSuccess = uploadToS3(context, imageUri, uploadUrl, imageType)

            if (uploadSuccess) {
                Log.d(TAG, "S3 업로드 성공: $imageKey")
                Result.success(imageKey)
            } else {
                Result.failure(Exception("S3 업로드 실패"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "이미지 업로드 오류", e)
            Result.failure(e)
        }
    }

    /**
     * S3에 직접 업로드
     */
    private suspend fun uploadToS3(
        context: Context,
        imageUri: Uri,
        uploadUrl: String,
        imageType: ImageContentType
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream = contentResolver.openInputStream(imageUri)
                ?: return@withContext false

            // InputStream을 ByteArray로 변환
            val imageBytes = inputStream.readBytes()
            inputStream.close()

            // Content-Type 설정
            val contentType = when (imageType) {
                ImageContentType.JPEG -> "image/jpeg"
                ImageContentType.PNG -> "image/png"
            }

            // OkHttp로 PUT 요청
            val requestBody = imageBytes.toRequestBody(contentType.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(uploadUrl)
                .put(requestBody)
                .addHeader("Content-Type", contentType)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val isSuccess = response.isSuccessful

            Log.d(TAG, "S3 업로드 응답: ${response.code}")
            response.close()

            isSuccess

        } catch (e: Exception) {
            Log.e(TAG, "S3 업로드 오류", e)
            false
        }
    }

    /**
     * URI에서 이미지 타입 추출
     */
    private fun getImageType(context: Context, uri: Uri): ImageContentType {
        val mimeType = context.contentResolver.getType(uri)
        return when {
            mimeType?.contains("png") == true -> ImageContentType.PNG
            else -> ImageContentType.JPEG
        }
    }

    companion object {
        private const val TAG = "PresignedImageRepo"
    }
}