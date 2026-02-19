package com.example.momenty.domain.member

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.repository.PresignedImageRepository
import com.example.momenty.data.repository.ProfileRepository
import com.example.momenty.global.security.TokenManager
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
        val accessToken: String?,
        val refreshToken: String?
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
    private val presignedImageRepository: PresignedImageRepository,
    private val tokenManager: TokenManager
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
     * 사용자 프로필 수정 - PATCH /api/mypage
     * 전달하지 않은 필드는 기존 값 유지
     */
    fun updateUserProfile(
        username: String? = null,
        gender: String? = null,
        birth: String? = null,
        profileUrl: String? = null,
        questTime: String? = null,
        resetQuestTime: Boolean? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.updateUserProfile(
                username = username,
                gender = gender,
                birth = birth,
                profileUrl = profileUrl,
                questTime = questTime,
                resetQuestTime = resetQuestTime
            )) {
                is ProfileRepository.ProfileResult.Success -> {

                    if (result.accessToken != null && result.refreshToken != null) {
                        tokenManager.saveTokens(
                            accessToken = result.accessToken,
                            refreshToken = result.refreshToken
                        )
                    }
                    _uiState.value = ProfileUiState.Success(
                        accessToken = result.accessToken,
                        refreshToken = result.refreshToken
                    )
                }
                is ProfileRepository.ProfileResult.Error -> {
                    _uiState.value = ProfileUiState.Error(getErrorMessage(result))
                }
            }
        }
    }

    /**
     * 반려동물 프로필 수정
     * 전달하지 않은 필드는 기존 값 유지
     */
    fun updatePetProfile(
        petId: Long,
        profileImageUrl: String? = null,  // 빈 배열이면 미설정
        petName: String? = null,
        gender: String? = null,           // "MALE", "FEMALE"
        birth: String? = null,            // "2000-01-01" 형식
        species: String? = null,          // "CAT" or "DOG"
        breedId: Long? = null,
        intro: String? = null             // 빈 배열이면 미설정
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.updatePetProfile(
                petId = petId,
                profileImageUrl = profileImageUrl,
                petName = petName,
                gender = gender,
                birth = birth,
                species = species,
                breedId = breedId,
                intro = intro
            )) {
                is ProfileRepository.ProfileResult.Success -> {
                    // 반려동물 수정은 토큰 반환 없음, 단순 성공 처리
                    _uiState.value = ProfileUiState.Success(
                        accessToken = null,
                        refreshToken = null
                    )
                }
                is ProfileRepository.ProfileResult.Error -> {
                    _uiState.value = ProfileUiState.Error(getErrorMessage(result))
                }
            }
        }
    }

    fun addPet(
        profileImageUrl: String? = null,
        petName: String,
        gender: String,
        birth: String,
        species: String,
        breedId: Long? = null,
        intro: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            when (val result = profileRepository.addPet(
                profileImageUrl = profileImageUrl,
                petName = petName,
                gender = gender,
                birth = birth,
                species = species,
                breedId = breedId,
                intro = intro
            )) {
                is ProfileRepository.ProfileResult.Success -> {
                    _uiState.value = ProfileUiState.Success(
                        accessToken = null,
                        refreshToken = null
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