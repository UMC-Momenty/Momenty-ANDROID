package com.example.momenty.domain.record

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.R
import com.example.momenty.data.remote.moment.EmotionDto
import com.example.momenty.data.repository.MomentRepository
import com.example.momenty.domain.record.write.RecordItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RecordViewModel @Inject constructor(
    private val momentRepository: MomentRepository
) : ViewModel() {



    private val _items = MutableStateFlow<List<RecordItem>>(emptyList())
    val items: StateFlow<List<RecordItem>> = _items

    fun loadMoments(userId: Long, petId: Long) {
        viewModelScope.launch {
            runCatching {
                momentRepository.getMomentList(userId = userId, petId = petId, page = 0, size = 20)
            }.onSuccess { res ->
                val mapped = res.result?.moments?.map { dto ->
                    RecordItem(
                        date = dto.createdAt.toKoreanDateOrFallback(),
                        title = dto.emotion.toTitle(),
                        mood = dto.emotion.toChip(),
                        content = dto.content,
                        imageRes = R.drawable.dummy1 //일단은 서버에서 안내오고 있어서 밴드 붙였습니다
                    )
                }

                _items.value = if (res.isSuccess && mapped != null) mapped else emptyList()
            }.onFailure { e ->
                e.printStackTrace()
                _items.value = emptyList()
            }

        }
    }

    fun createMoment(
        userId: Long,
        petId: Long,
        emotion: EmotionDto,
        content: String,
        imageUris: List<Uri>,
        contentResolver: android.content.ContentResolver,
        onSuccess: () -> Unit,
        onFail: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                momentRepository.createMomentWithImages(
                    userId = userId,
                    petId = petId,
                    emotion = emotion,
                    content = content,
                    imageUris = imageUris,
                    contentResolver = contentResolver
                )
            }.onSuccess { res ->
                if (res.isSuccess) onSuccess() else onFail(IllegalStateException(res.message))
            }.onFailure { e ->
                onFail(e)
            }
        }
    }

}

private fun String.toKoreanDateOrFallback(): String {
    return runCatching {
        val datePart = this.substring(0, 10) // yyyy-MM-dd
        val (y, m, d) = datePart.split("-")
        "$y.$m.$d"
    }.getOrDefault(this)
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

