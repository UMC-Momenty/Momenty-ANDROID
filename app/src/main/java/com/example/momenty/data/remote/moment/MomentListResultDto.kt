package com.example.momenty.data.remote.moment

import com.google.gson.annotations.SerializedName

data class MomentListResultDto(
    @SerializedName("moments")
    val moments: List<MomentItemDto>,
    @SerializedName("pageInfo")
    val pageInfo: PageInfoDto,
)

data class MomentItemDto(
    @SerializedName("momentId") val momentId: Long,
    @SerializedName("emotion") val emotion: EmotionDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("content") val content: String,
    @SerializedName("imageUrl") val imageUrl: String? = null
)



enum class EmotionDto {
    HAPPINESS,
    SADNESS,
    NEUTRAL
}

data class PageInfoDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("totalElements")
    val totalElements: Long,
    @SerializedName("hasNext")
    val hasNext: Boolean,
    @SerializedName("hasPrevious")
    val hasPrevious: Boolean,
)

data class PresignedRequestDto(
    val imageTypes: List<String>
)

data class PresignedUrlDto(val key: String, val url: String)

data class CreateMomentRequestDto(
    val images: List<MomentImageKeyDto>,
    val emotion: EmotionDto,
    val content: String,
)

data class MomentImageKeyDto(
    val imageKey: String
)

enum class ImageTypeDto { JPEG, PNG }


