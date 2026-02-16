package com.example.momenty.domain.record.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.remote.moment.EmotionDto
import com.example.momenty.data.repository.MomentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.momenty.global.util.toKoreanDateOrFallback


@HiltViewModel
class MomentDetailViewModel @Inject constructor(
    private val momentRepository: MomentRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<MomentDetailUiModel?>(null)
    val ui: StateFlow<MomentDetailUiModel?> = _ui

    fun load(userId: Long, petId: Long, momentId: Long, createdAt: String) {
        viewModelScope.launch {
            runCatching {
                momentRepository.getMomentDetail(userId, petId, momentId)
            }.onSuccess { res ->
                if (!res.isSuccess || res.result == null) {
                    _ui.value = null
                    return@onSuccess
                }

                val dto = res.result
                val content = dto.content.orEmpty()

                val (title, body) = splitTitleBody(content)


                val imageUrls = if (dto.images.isNotEmpty()) {
                    dto.images.mapIndexed { idx, _ -> "https://picsum.photos/600/600?random=$idx" }
                } else emptyList()

                _ui.value = MomentDetailUiModel(
                    imageUrls = imageUrls,
                    dateText = createdAt.toKoreanDateOrFallback(),
                    moodChipText = dto.emotion.toChip(),
                    moodTitleText = dto.emotion.toTitle(),
                    titleText = title,
                    bodyText = body,
                    emotion = dto.emotion
                )
            }.onFailure {
                it.printStackTrace()
                _ui.value = null
            }
        }
    }

    private fun splitTitleBody(content: String): Pair<String, String> {
        val lines = content.split("\n")
        val title = lines.firstOrNull()?.trim().orEmpty()
        val body = lines.drop(1).joinToString("\n").trim()
        return if (title.isBlank()) "제목 없음" to content else title to body
    }
}

private fun EmotionDto.toTitle(): String = when (this) {
    EmotionDto.HAPPINESS -> "즐거운 날"
    EmotionDto.SADNESS -> "슬픈 날"
    EmotionDto.NEUTRAL -> "보통인 날"
}

private fun EmotionDto.toChip(): String = when (this) {
    EmotionDto.HAPPINESS -> "즐거움"
    EmotionDto.SADNESS -> "슬픔"
    EmotionDto.NEUTRAL -> "보통"
}
