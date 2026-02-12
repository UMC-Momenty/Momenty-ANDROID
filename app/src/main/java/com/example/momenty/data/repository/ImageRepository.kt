package com.example.momenty.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.momenty.data.api.ImageUploadApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이미지 업로드 Repository
 */
@Singleton
class ImageRepository @Inject constructor(
    private val imageUploadApiService: ImageUploadApiService
) {

    /**
     * 이미지를 서버에 업로드하고 URL을 반환
     * @param context Context
     * @param imageUri 업로드할 이미지 URI
     * @return 업로드된 이미지 URL 또는 null
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // URI에서 파일로 변환
                val file = uriToFile(context, imageUri)
                    ?: return@withContext Result.failure(Exception("파일 변환 실패"))

                // Multipart 생성
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // API 호출
                val response = imageUploadApiService.uploadImage(body)

                // 임시 파일 삭제
                file.delete()

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val imageUrl = response.body()?.result?.imageUrl
                    if (imageUrl != null) {
                        Result.success(imageUrl)
                    } else {
                        Result.failure(Exception("이미지 URL을 받지 못했습니다"))
                    }
                } else {
                    Result.failure(Exception("이미지 업로드 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e("ImageRepository", "이미지 업로드 오류", e)
                Result.failure(e)
            }
        }

    /**
     * URI를 File로 변환
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "temp_image_${System.currentTimeMillis()}.jpg"

            val tempFile = File(context.cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ImageRepository", "URI to File 변환 오류", e)
            null
        }
    }

    /**
     * URI에서 파일명 추출
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}