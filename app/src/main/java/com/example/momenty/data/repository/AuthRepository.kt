package com.example.momenty.data.repository

import android.content.Context
import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.auth.KakaoLoginRequest
import com.example.momenty.global.api.ApiException
import com.example.momenty.global.security.TokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kakao.sdk.auth.model.OAuthToken
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

    /**
     * 카카오 로그인 전체 플로우
     * 1. 카카오 SDK로 로그인 → Access Token 획득
     * 2. Spring 백엔드로 Token 전송 → JWT + Firebase Custom Token 수신
     * 3. JWT 토큰 저장
     * 4. Firebase Auth에 Custom Token으로 로그인
     */
    suspend fun loginWithKakao(): AuthResult {
        return try {
            // 1. 카카오 로그인 (Access Token 획득)
            val kakaoToken = kakaoLogin()

            // 2. 백엔드로 토큰 전송 및 JWT + Firebase Custom Token 받기
            val response = authApi.kakaoLogin(
                KakaoLoginRequest(kakaoToken.accessToken)
            )

            // 응답 검증
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
            AuthResult.Error(
                message = e.message,
                code = e.code
            )
        } catch (e: Exception) {
            AuthResult.Error(
                message = e.message ?: "로그인 중 오류가 발생했습니다"
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
                    continuation.resumeWithException(error)
                }
                token != null -> {
                    continuation.resume(token)
                }
                else -> {
                    continuation.resumeWithException(
                        Exception("카카오 토큰을 받지 못했습니다")
                    )
                }
            }
        }

        // 카카오톡 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    // 카카오톡 로그인 실패 시 카카오 계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    callback(token, null)
                }
            }
        } else {
            // 카카오 계정으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * Firebase Custom Token으로 로그인
     */
    private suspend fun signInWithFirebase(customToken: String): FirebaseUser {
        val authResult = firebaseAuth.signInWithCustomToken(customToken).await()
        return authResult.user ?: throw Exception("Firebase 사용자 정보를 가져올 수 없습니다")
    }

    /**
     * 현재 로그인된 사용자 정보
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn() && getCurrentUser() != null
    }

    /**
     * 로그아웃
     */
    fun logout() {
        // 카카오 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                android.util.Log.e("AuthRepository", "카카오 로그아웃 실패", error)
            }
        }

        // JWT 토큰 삭제
        tokenManager.clearTokens()

        // Firebase 로그아웃
        firebaseAuth.signOut()
    }

    /**
     * 회원 탈퇴
     */
    suspend fun withdraw(): Result<Unit> {
        return try {
            // 카카오 연결 끊기
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    android.util.Log.e("AuthRepository", "카카오 연결 끊기 실패", error)
                }
            }

            // Firebase 계정 삭제
            getCurrentUser()?.delete()?.await()

            // 토큰 삭제
            tokenManager.clearTokens()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}