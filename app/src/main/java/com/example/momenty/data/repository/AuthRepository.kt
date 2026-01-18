// data/repository/AuthRepository.kt
package com.example.momenty.data.repository

import android.content.Context
import android.util.Log
import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.auth.KakaoLoginRequest
import com.example.momenty.global.api.ApiException
import com.example.momenty.global.security.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String, val code: String? = null) : AuthResult()
}

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val USE_MOCK = true  // ← 백엔드 준비되면 false로 변경
    }

    /**
     * 카카오 로그인 전체 플로우
     */
    suspend fun loginWithKakao(): AuthResult {
        return try {
            // 1. 카카오 로그인 (Access Token 획득)
            val kakaoToken = kakaoLogin()
            Log.d(TAG, "카카오 토큰 획득 성공")

            if (USE_MOCK) {
                // Mock: 백엔드 없이 Firebase만 사용
                return loginWithFirebaseMock()
            }

            // 2. 백엔드로 토큰 전송 (실제 구현)
            val response = authApi.kakaoLogin(
                KakaoLoginRequest(kakaoToken.accessToken)
            )

            if (!response.isSuccess || response.result == null) {
                return AuthResult.Error(
                    message = response.message,
                    code = response.code
                )
            }

            val loginData = response.result

            // 3. JWT 토큰 저장
            tokenManager.saveAccessToken(loginData.accessToken)
            tokenManager.saveRefreshToken(loginData.refreshToken)

            // 4. Firebase 로그인
            val firebaseUser = signInWithFirebase(loginData.firebaseCustomToken)

            AuthResult.Success(firebaseUser)

        } catch (e: ApiException) {
            Log.e(TAG, "API 에러", e)
            AuthResult.Error(
                message = e.message,
                code = e.code
            )
        } catch (e: Exception) {
            Log.e(TAG, "로그인 실패", e)
            AuthResult.Error(
                message = e.message ?: "로그인 중 오류가 발생했습니다"
            )
        }
    }

    /**
     * Mock: 백엔드 없이 Firebase 익명 로그인
     */
    private suspend fun loginWithFirebaseMock(): AuthResult {
        return try {
            Log.d(TAG, "Mock 모드: Firebase 익명 로그인 시도")

            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Firebase 사용자 정보를 가져올 수 없습니다")

            // Mock JWT 토큰 저장
            tokenManager.saveAccessToken("mock_access_token_${System.currentTimeMillis()}")
            tokenManager.saveRefreshToken("mock_refresh_token_${System.currentTimeMillis()}")

            Log.d(TAG, "Mock 로그인 성공: ${user.uid}")
            AuthResult.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Mock 로그인 실패", e)
            AuthResult.Error(
                message = e.message ?: "로그인에 실패했습니다"
            )
        }
    }

    /**
     * 카카오 SDK를 통한 로그인
     */
    private suspend fun kakaoLogin(): OAuthToken = suspendCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    Log.e(TAG, "카카오 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(
                            Exception("로그인이 취소되었습니다")
                        )
                    } else {
                        continuation.resumeWithException(error)
                    }
                }
                token != null -> {
                    Log.d(TAG, "카카오 로그인 성공")
                    continuation.resume(token)
                }
                else -> {
                    continuation.resumeWithException(
                        Exception("카카오 토큰을 받지 못했습니다")
                    )
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            Log.d(TAG, "카카오톡으로 로그인 시도")
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    error != null -> {
                        Log.w(TAG, "카카오톡 로그인 실패, 카카오 계정으로 재시도", error)

                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            callback(null, error)
                        } else {
                            Log.d(TAG, "카카오 계정으로 로그인 시도")
                            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                        }
                    }
                    token != null -> {
                        callback(token, null)
                    }
                    else -> {
                        callback(null, Exception("카카오톡 로그인 실패"))
                    }
                }
            }
        } else {
            Log.d(TAG, "카카오 계정으로 로그인 시도")
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * Firebase Custom Token으로 로그인
     */
    private suspend fun signInWithFirebase(customToken: String): FirebaseUser {
        Log.d(TAG, "Firebase 로그인 시도")
        val authResult = firebaseAuth.signInWithCustomToken(customToken).await()
        val user = authResult.user ?: throw Exception("Firebase 사용자 정보를 가져올 수 없습니다")
        Log.d(TAG, "Firebase 로그인 성공: ${user.uid}")
        return user
    }

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn() && getCurrentUser() != null
    }

    fun logout() {
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG, "카카오 로그아웃 실패", error)
            } else {
                Log.d(TAG, "카카오 로그아웃 성공")
            }
        }

        tokenManager.clearTokens()
        firebaseAuth.signOut()
        Log.d(TAG, "로그아웃 완료")
    }

    suspend fun withdraw(): Result<Unit> {
        return try {
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.e(TAG, "카카오 연결 끊기 실패", error)
                } else {
                    Log.d(TAG, "카카오 연결 끊기 성공")
                }
            }

            getCurrentUser()?.delete()?.await()
            tokenManager.clearTokens()

            Log.d(TAG, "회원 탈퇴 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "회원 탈퇴 실패", e)
            Result.failure(e)
        }
    }
}