package com.example.momenty.data.repository

import android.content.Context
import android.util.Log
import com.example.momenty.BuildConfig
import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.auth.SocialLoginRequest
import com.example.momenty.global.api.ApiException
import com.example.momenty.global.security.TokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 인증 관련 Repository
 *
 * Firebase는 FCM 용도로만 사용
 * 인증은 백엔드 JWT만 사용
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val LOGIN_TIMEOUT_MS = 30_000L // 30초
    }

    /**
     * Google Sign-In Client (Lazy 초기화)
     * Firebase용이 아닌 구글 로그인용
     */
    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.momenty.R.string.default_web_client_id))  // JWT idToken 획득용
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    /**
     * 구글 로그인 전체 플로우
     *
     * @param account Google Sign-In으로 받은 계정 정보
     * @return AuthResult 로그인 성공 또는 실패 결과
     */
    suspend fun loginWithGoogle(account: GoogleSignInAccount): AuthResult {
        return try {
            withTimeout(LOGIN_TIMEOUT_MS) {
                logLoginAttempt("Google", account.email)

                // 1. Google ID Token 획득
                val idToken = account.idToken
                    ?: return@withTimeout AuthResult.Error.Unknown("Google ID Token을 가져올 수 없습니다")

                // 2. 백엔드로 ID Token 전송
                val response = authApi.socialLogin(
                    SocialLoginRequest(
                        provider = "GOOGLE",
                        accessToken = idToken  // 구글은 idToken 전송
                    )
                )

                // 3. 응답 검증
                if (!response.isSuccess || response.result == null) {
                    return@withTimeout AuthResult.Error.Api(
                        response.message,
                        response.code
                    )
                }

                // 4. 백엔드 JWT 토큰 저장
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken
                )

                Log.d(TAG, "Google 로그인 성공: ${account.displayName}")

                // 5. 성공 반환 (사용자 정보 포함)
                AuthResult.Success(
                    userName = account.displayName,
                    email = account.email
                )
            }
        } catch (e: Exception) {
            handleLoginError("Google", e)
        }
    }

    /**
     * 카카오 로그인 전체 플로우
     *
     * @return AuthResult 로그인 성공 또는 실패 결과
     */
    suspend fun loginWithKakao(): AuthResult {
        return try {
            withTimeout(LOGIN_TIMEOUT_MS) {
                logLoginAttempt("Kakao", null)

                // 1. 카카오 SDK를 통한 로그인
                val kakaoToken = kakaoLogin()
                Log.d(TAG, "카카오 토큰 획득 성공")

                // 2. 사용자 정보 가져오기
                val userInfo = getKakaoUserInfo()

                // 3. 백엔드로 Access Token 전송
                val response = authApi.socialLogin(
                    SocialLoginRequest(
                        provider = "KAKAO",
                        accessToken = kakaoToken.accessToken  // 카카오는 accessToken 전송
                    )
                )

                // 4. 응답 검증
                if (!response.isSuccess || response.result == null) {
                    return@withTimeout AuthResult.Error.Api(
                        response.message,
                        response.code
                    )
                }

                // 5. 백엔드 JWT 토큰 저장
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken
                )

                Log.d(TAG, "Kakao 로그인 성공: ${userInfo.nickname}")

                // 6. 성공 반환
                AuthResult.Success(
                    userName = userInfo.nickname,
                    email = userInfo.email
                )
            }
        } catch (e: Exception) {
            handleLoginError("Kakao", e)
        }
    }

    /**
     * 네이버 로그인 전체 플로우
     *
     * @return AuthResult 로그인 성공 또는 실패 결과
     */
    suspend fun loginWithNaver(): AuthResult {
        return try {
            withTimeout(LOGIN_TIMEOUT_MS) {
                logLoginAttempt("Naver", null)

                // 1. 네이버 Access Token 획득
                val naverToken = getNaverAccessToken()
                Log.d(TAG, "네이버 토큰 획득 성공")

                // 2. 사용자 정보 가져오기
                val userInfo = getNaverUserInfo()

                // 3. 백엔드로 Access Token 전송
                val response = authApi.socialLogin(
                    SocialLoginRequest(
                        provider = "NAVER",
                        accessToken = naverToken.accessToken  // 네이버는 accessToken 전송
                    )
                )

                // 4. 응답 검증
                if (!response.isSuccess || response.result == null) {
                    return@withTimeout AuthResult.Error.Api(
                        response.message,
                        response.code
                    )
                }

                // 5. 백엔드 JWT 토큰 저장
                tokenManager.saveTokens(
                    accessToken = response.result.accessToken,
                    refreshToken = response.result.refreshToken
                )

                Log.d(TAG, "Naver 로그인 성공: ${userInfo.name}")

                // 6. 성공 반환
                AuthResult.Success(
                    userName = userInfo.name,
                    email = userInfo.email
                )
            }
        } catch (e: Exception) {
            handleLoginError("Naver", e)
        }
    }

    /**
     * 카카오 로그인 실행
     */
    private suspend fun kakaoLogin(): OAuthToken = suspendCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(
                            UserCancellationException("카카오 로그인이 취소되었습니다")
                        )
                    } else {
                        continuation.resumeWithException(error)
                    }
                }
                token != null -> continuation.resume(token)
                else -> continuation.resumeWithException(
                    IllegalStateException("카카오 로그인 결과가 없습니다")
                )
            }
        }

        // 카카오톡 앱 설치 여부에 따라 로그인 방식 선택
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * 카카오 사용자 정보 가져오기
     */
    private suspend fun getKakaoUserInfo(): KakaoUserInfo = suspendCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            when {
                error != null -> continuation.resumeWithException(error)
                user != null -> continuation.resume(
                    KakaoUserInfo(
                        id = user.id.toString(),
                        nickname = user.kakaoAccount?.profile?.nickname,
                        email = user.kakaoAccount?.email
                    )
                )
                else -> continuation.resumeWithException(
                    IllegalStateException("카카오 사용자 정보를 가져올 수 없습니다")
                )
            }
        }
    }

    /**
     * 네이버 Access Token 가져오기
     */
    private fun getNaverAccessToken(): NaverToken {
        val accessToken = NaverIdLoginSDK.getAccessToken()
            ?: throw IllegalStateException("네이버 액세스 토큰을 가져올 수 없습니다")

        val tokenType = NaverIdLoginSDK.getTokenType() ?: "Bearer"

        return NaverToken(accessToken, tokenType)
    }

    /**
     * 네이버 사용자 정보 가져오기
     */
    private suspend fun getNaverUserInfo(): NaverUserInfo = suspendCoroutine { continuation ->
        NidOAuthLogin().callProfileApi(object : com.navercorp.nid.profile.NidProfileCallback<com.navercorp.nid.profile.data.NidProfileResponse> {
            override fun onSuccess(result: com.navercorp.nid.profile.data.NidProfileResponse) {
                val profile = result.profile
                if (profile != null) {
                    continuation.resume(
                        NaverUserInfo(
                            id = profile.id ?: "",
                            name = profile.name,
                            email = profile.email
                        )
                    )
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("네이버 사용자 정보를 가져올 수 없습니다")
                    )
                }
            }

            override fun onError(errorCode: Int, message: String) {
                continuation.resumeWithException(
                    Exception("네이버 사용자 정보 조회 실패: $message")
                )
            }

            override fun onFailure(httpStatus: Int, message: String) {
                continuation.resumeWithException(
                    Exception("네이버 사용자 정보 조회 실패: $message")
                )
            }
        })
    }

    /**
     * 현재 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * 로그아웃
     * 모든 플랫폼에서 로그아웃을 시도하며, 일부 실패해도 계속 진행
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val errors = mutableListOf<Throwable>()

            // 1. 카카오 로그아웃
            runCatching {
                suspendCoroutine<Unit> { continuation ->
                    UserApiClient.instance.logout { error ->
                        if (error != null) {
                            Log.e(TAG, "카카오 로그아웃 실패: ${error.message}")
                            errors.add(error)
                        } else {
                            Log.d(TAG, "카카오 로그아웃 성공")
                        }
                        continuation.resume(Unit)
                    }
                }
            }.onFailure {
                Log.e(TAG, "카카오 로그아웃 처리 중 오류", it)
                errors.add(it)
            }

            // 2. Google 로그아웃
            runCatching {
                googleSignInClient.signOut()
                Log.d(TAG, "Google 로그아웃 성공")
            }.onFailure {
                Log.e(TAG, "Google 로그아웃 실패", it)
                errors.add(it)
            }

            // 3. 네이버 로그아웃
            runCatching {
                NaverIdLoginSDK.logout()
                Log.d(TAG, "네이버 로그아웃 성공")
            }.onFailure {
                Log.e(TAG, "네이버 로그아웃 실패", it)
                errors.add(it)
            }

            // 4. 토큰 정리 (항상 실행)
            tokenManager.clearTokens()

            if (errors.isNotEmpty()) {
                Log.w(TAG, "로그아웃 완료 (일부 오류 발생: ${errors.size}개)")
            } else {
                Log.d(TAG, "로그아웃 완료")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "로그아웃 중 심각한 오류 발생", e)

            // 심각한 오류가 발생해도 최소한의 정리는 수행
            try {
                tokenManager.clearTokens()
            } catch (cleanupError: Exception) {
                Log.e(TAG, "정리 작업 실패", cleanupError)
            }

            Result.failure(e)
        }
    }

    /**
     * 회원 탈퇴
     * 모든 플랫폼에서 계정을 삭제하고 토큰을 정리
     */
    suspend fun withdraw(): Result<Unit> {
        return try {
            val errors = mutableListOf<Throwable>()

            // 1. 카카오 연결 끊기
            runCatching {
                suspendCoroutine<Unit> { continuation ->
                    UserApiClient.instance.unlink { error ->
                        if (error != null) {
                            Log.e(TAG, "카카오 연결 끊기 실패: ${error.message}")
                            errors.add(error)
                        } else {
                            Log.d(TAG, "카카오 연결 끊기 성공")
                        }
                        continuation.resume(Unit)
                    }
                }
            }.onFailure {
                Log.e(TAG, "카카오 연결 끊기 처리 중 오류", it)
                errors.add(it)
            }

            // 2. Google 로그아웃 (연결 끊기는 별도 API 필요)
            runCatching {
                googleSignInClient.signOut()
                Log.d(TAG, "Google 로그아웃 성공")
            }.onFailure {
                Log.e(TAG, "Google 로그아웃 실패", it)
                errors.add(it)
            }

            // 3. 네이버 연결 끊기
            runCatching {
                suspendCoroutine<Unit> { continuation ->
                    NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
                        override fun onSuccess() {
                            Log.d(TAG, "네이버 연결 끊기 성공")
                            continuation.resume(Unit)
                        }

                        override fun onFailure(httpStatus: Int, message: String) {
                            val error = Exception("네이버 연결 끊기 실패: $message")
                            Log.e(TAG, error.message, error)
                            errors.add(error)
                            continuation.resume(Unit)
                        }

                        override fun onError(errorCode: Int, message: String) {
                            val error = Exception("네이버 연결 끊기 오류: $message")
                            Log.e(TAG, error.message, error)
                            errors.add(error)
                            continuation.resume(Unit)
                        }
                    })
                }
            }.onFailure {
                Log.e(TAG, "네이버 연결 끊기 처리 중 오류", it)
                errors.add(it)
            }

            // 4. 토큰 정리 (항상 실행)
            tokenManager.clearTokens()

            if (errors.isNotEmpty()) {
                Log.w(TAG, "회원 탈퇴 완료 (일부 오류 발생: ${errors.size}개)")
                return Result.failure(
                    Exception("회원 탈퇴 중 일부 오류 발생: ${errors.size}개")
                )
            }

            Log.d(TAG, "회원 탈퇴 완료")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "회원 탈퇴 실패", e)

            // 실패해도 최소한의 정리는 수행
            try {
                tokenManager.clearTokens()
            } catch (cleanupError: Exception) {
                Log.e(TAG, "정리 작업 실패", cleanupError)
            }

            Result.failure(e)
        }
    }

    // ========== Helper Functions ==========

    /**
     * 로그인 시도 로깅 (민감한 정보 제외)
     */
    private fun logLoginAttempt(provider: String, identifier: String?) {
        if (BuildConfig.DEBUG && identifier != null) {
            // 개발 환경에서만 이메일 일부 로깅
            val masked = identifier.take(3) + "***"
            Log.d(TAG, "$provider 로그인 시도: $masked")
        } else {
            Log.d(TAG, "$provider 로그인 시도")
        }
    }

    /**
     * 통합 에러 핸들링
     */
    private fun handleLoginError(provider: String, error: Exception): AuthResult.Error {
        Log.e(TAG, "$provider 로그인 실패", error)

        return when (error) {
            is ApiException -> {
                AuthResult.Error.Api(error.message, error.code)
            }
            is UserCancellationException -> {
                AuthResult.Error.Cancelled()
            }
            is kotlinx.coroutines.TimeoutCancellationException -> {
                AuthResult.Error.Timeout()
            }
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> {
                AuthResult.Error.Network("네트워크 연결을 확인해주세요")
            }
            else -> {
                AuthResult.Error.Unknown(
                    message = error.message ?: "$provider 로그인 중 오류가 발생했습니다",
                    throwable = error
                )
            }
        }
    }

    /**
     * 리소스 정리 (필요시 호출)
     */
    fun cleanup() {
        Log.d(TAG, "AuthRepository 리소스 정리")
    }
}

/**
 * 카카오 사용자 정보
 */
data class KakaoUserInfo(
    val id: String,
    val nickname: String?,
    val email: String?
)

/**
 * 네이버 사용자 정보
 */
data class NaverUserInfo(
    val id: String,
    val name: String?,
    val email: String?
)