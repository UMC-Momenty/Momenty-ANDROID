package com.example.momenty.domain.member.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.momenty.data.repository.AuthRepository
import com.example.momenty.data.repository.AuthResult
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String, val code: String? = null) : LoginState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    /**
     * 카카오 로그인 시작
     */
    fun loginWithKakao() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            when (val result = authRepository.loginWithKakao()) {
                is AuthResult.Success -> {
                    _loginState.value = LoginState.Success(result.user)
                }
                is AuthResult.Error -> {
                    _loginState.value = LoginState.Error(
                        message = result.message,
                        code = result.code
                    )
                }
            }
        }
    }

    /**
     * 현재 사용자 확인
     */
    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }

    /**
     * 로그아웃
     */
    fun logout() {
        authRepository.logout()
    }

    /**
     * 회원 탈퇴
     */
    fun withdraw() {
        viewModelScope.launch {
            authRepository.withdraw()
        }
    }

    /**
     * 상태 초기화
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}