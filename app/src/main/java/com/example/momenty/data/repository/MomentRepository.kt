package com.example.momenty.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.momenty.data.remote.S3Uploader
import com.example.momenty.data.remote.moment.CreateMomentRequestDto
import com.example.momenty.data.remote.moment.EmotionDto
import com.example.momenty.data.remote.moment.ImageTypeDto
import com.example.momenty.data.remote.moment.MomentImageKeyDto
import com.example.momenty.data.remote.moment.MomentListResultDto
import com.example.momenty.data.remote.moment.MomentsApi
import com.example.momenty.data.remote.moment.PresignedRequestDto
import com.example.momenty.global.api.BaseResponse
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MomentRepository @Inject constructor(
    private val momentsApi: MomentsApi,
) {
    suspend fun getMomentList(
        userId: Long,
        petId: Long,
        page: Int = 0,
        size: Int = 10,
    ): BaseResponse<MomentListResultDto> {
        return momentsApi.getMomentsByPet(
            userId = userId,
            petId = petId,
            page = page,
            size = size,
            sort = listOf("createdAt,DESC")
        )
    }

    suspend fun createMomentWithImages(
        userId: Long,
        petId: Long,
        emotion: EmotionDto,
        content: String,
        imageUris: List<Uri>,
        contentResolver: ContentResolver
    ): BaseResponse<String> {

        if (imageUris.isEmpty()) throw IllegalArgumentException("사진 1장 이상 필요")

        val safeUris = imageUris.take(5)
        val imageCount = safeUris.size


        val mimeTypes = safeUris.map { uri ->
            contentResolver.getType(uri) ?: "image/jpeg"
        }


        val imageTypes = mimeTypes.map { mime ->
            if (mime == "image/png") "PNG" else "JPEG"
        }

        val presignedRes =
            momentsApi.createMomentImagePresignedUrl(
                PresignedRequestDto(imageTypes)
            )


        val presignedAll = presignedRes.result.orEmpty()
        val presigned = presignedAll.take(imageCount)

        if (!presignedRes.isSuccess || presigned.size != imageCount) {
            throw IllegalStateException("presigned 발급 실패 or 개수 불일치")
        }



        if (!com.example.momenty.global.mock.MockApiInterceptor.isMockEnabled) {
            presigned.forEachIndexed { idx, item ->
                val uri = safeUris[idx]
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("이미지 읽기 실패: $uri")

                val contentType = mimeTypes[idx]

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.momenty.data.remote.S3Uploader.upload(
                        presignedUrl = item.url,
                        bytes = bytes,
                        contentType = contentType
                    )
                }
            }
        } else {
            android.util.Log.d("MockApi", "SKIP S3 upload (mock mode)")
        }


        // 4) 모먼트 생성
        val body = CreateMomentRequestDto(
            images = presigned.map { MomentImageKeyDto(it.key) },
            emotion = emotion,
            content = content
        )

        return momentsApi.createMoment(userId, petId, body)
    }


}
