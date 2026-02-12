package com.example.momenty.domain.member

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.repository.PresignedImageRepository
import com.example.momenty.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 프로필 화면 UI 상태
 */
sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(
        val userId: String,
        val userName: String,
        val petId: String,
        val petName: String
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * 이미지 업로드 상태
 */
sealed class ImageUploadState {
    object Idle : ImageUploadState()
    object Loading : ImageUploadState()
    data class Success(val imageKey: String) : ImageUploadState()  // ✅ imageUrl → imageKey
    data class Error(val message: String) : ImageUploadState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val presignedImageRepository: PresignedImageRepository  // ✅ 변경
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _imageUploadState = MutableStateFlow<ImageUploadState>(ImageUploadState.Idle)
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    /**
     * Presigned URL 방식으로 이미지 업로드
     */
    fun uploadImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _imageUploadState.value = ImageUploadState.Loading

            presignedImageRepository.uploadImageWithPresignedUrl(context, imageUri)
                .onSuccess { imageKey ->
                    _imageUploadState.value = ImageUploadState.Success(imageKey)  // ✅ imageKey 반환
                }
                .onFailure { exception ->
                    _imageUploadState.value = ImageUploadState.Error(
                        exception.message ?: "이미지 업로드 실패"
                    )
                }
        }
    }

    /**
     * 프로필 업데이트
     */
    fun updateProfile(
        userName: String,
        userGender: String,
        userBirthDate: String,
        userImageKey: String?,      // ✅ userProfileUrl → userImageKey
        alarmTime: String?,
        petName: String,
        petGender: String,
        petBirthDate: String,
        petImageKey: String?,       // ✅ petProfileUrl → petImageKey
        petType: String,
        petBreed: String?,
        petIntroduction: String?
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.updateProfile(
                userName = userName,
                userGender = userGender,
                userBirthDate = userBirthDate,
                userImageKey = userImageKey,      // ✅ 변경
                alarmTime = alarmTime,
                petName = petName,
                petGender = petGender,
                petBirthDate = petBirthDate,
                petImageKey = petImageKey,        // ✅ 변경
                petType = petType,
                petBreed = petBreed,
                petIntroduction = petIntroduction
            )) {
                is ProfileRepository.ProfileResult.Success -> {
                    _uiState.value = ProfileUiState.Success(
                        userId = result.userId,
                        userName = result.userName,
                        petId = result.petId,
                        petName = result.petName
                    )
                }
                is ProfileRepository.ProfileResult.Error -> {
                    _uiState.value = ProfileUiState.Error(getErrorMessage(result))
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
        _imageUploadState.value = ImageUploadState.Idle
    }

    /**
     * 에러 메시지 생성
     */
    private fun getErrorMessage(error: ProfileRepository.ProfileResult.Error): String {
        return when (error) {
            is ProfileRepository.ProfileResult.Error.Network -> "네트워크 연결을 확인해주세요"
            is ProfileRepository.ProfileResult.Error.Api -> "서버 오류: ${error.message}"
            is ProfileRepository.ProfileResult.Error.Unauthorized -> "로그인이 필요합니다"
            is ProfileRepository.ProfileResult.Error.Unknown -> error.message
        }
    }
}