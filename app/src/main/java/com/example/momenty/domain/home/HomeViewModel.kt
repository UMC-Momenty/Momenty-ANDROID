package com.example.momenty.domain.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.remote.profile.PetApi
import com.example.momenty.data.remote.profile.PetSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val petApi: PetApi
) : ViewModel() {

    private val _petProfileImageUrl = MutableStateFlow<String?>(null)
    val petProfileImageUrl: StateFlow<String?> = _petProfileImageUrl

    fun loadMyPets() {
        viewModelScope.launch {
            try {
                val response = petApi.getMyPets()
                if (response.isSuccess && response.result != null) {
                    _petProfileImageUrl.value = response.result.firstOrNull()?.profileImageUrl
                }
            } catch (e: Exception) {
                // 이미지 로드 실패 시 기본 이미지 사용
            }
        }
    }
}