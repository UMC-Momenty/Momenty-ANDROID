// ui/auth/AuthViewModel.kt
package com.example.momenty.ui.auth

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.repository.AuthRepository
import com.example.momenty.data.repository.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 인증 화면 UI 상태
 */
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val userName: String?) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * 구글 로그인
     */
    fun startGoogleSignIn(launcher: ActivityResultLauncher<android.content.Intent>){
        val signInIntent = authRepository.googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            when (val result = authRepository.loginWithGoogle(account)) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.user.displayName)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(getErrorMessage(result))
                }
            }
        }
    }

    /**
     * 카카오 로그인
     */
    fun loginWithKakao() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            when (val result = authRepository.loginWithKakao()) {
                is AuthResult.Success -> {
                    _uiState.value = AuthUiState.Success(result.user.displayName)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState.Error(getErrorMessage(result))
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    /**
     * 에러 메시지 생성
     */
    private fun getErrorMessage(error: AuthResult.Error): String {
        return when (error) {
            is AuthResult.Error.Network -> "네트워크 연결을 확인해주세요"
            is AuthResult.Error.Api -> "서버 오류: ${error.message}"
            is AuthResult.Error.Firebase -> "인증 실패: ${error.message}"
            is AuthResult.Error.Cancelled -> "로그인이 취소되었습니다"
            is AuthResult.Error.Timeout -> "요청 시간이 초과되었습니다. 다시 시도해주세요"
            is AuthResult.Error.Unknown -> error.message
        }
    }
}