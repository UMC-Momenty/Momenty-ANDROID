// data/repository/AuthRepository.kt
package com.example.momenty.data.repository

import android.content.Context
import android.util.Log
import com.example.momenty.BuildConfig
import com.example.momenty.R
import com.example.momenty.data.remote.auth.AuthApi
import com.example.momenty.data.remote.auth.GoogleLoginRequest
import com.example.momenty.data.remote.auth.KakaoLoginRequest
import com.example.momenty.data.remote.auth.LoginResponse
import com.example.momenty.data.remote.auth.NaverLoginRequest
import com.example.momenty.global.api.ApiException
import com.example.momenty.global.api.BaseResponse
import com.example.momenty.global.security.TokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 인증 결과를 나타내는 Sealed Class
 */
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()

    sealed class Error(open val message: String) : AuthResult() {
        data class Network(override val message: String) : Error(message)
        data class Api(override val message: String, val code: String) : Error(message)
        data class Firebase(override val message: String) : Error(message)
        data class Cancelled(override val message: String = "로그인이 취소되었습니다") : Error(message)
        data class Timeout(override val message: String = "요청 시간이 초과되었습니다") : Error(message)
        data class Unknown(override val message: String, val throwable: Throwable? = null) : Error(message)
    }
}

/**
 * 네이버 토큰 정보
 */
data class NaverToken(
    val accessToken: String,
    val tokenType: String
)

/**
 * 인증 관련 Repository
 *
 * Google/Kakao 소셜 로그인 및 Firebase 인증을 처리합니다.
 * Mock 모드를 지원하여 백엔드 없이도 개발이 가능합니다.
 */
@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth
) {

    companion object {
        private const val TAG = "AuthRepository"
        private const val LOGIN_TIMEOUT_MS = 30_000L // 30초

        // BuildConfig에서 관리 (프로덕션에서는 항상 false)
        private val USE_MOCK = BuildConfig.DEBUG
    }

    /**
     * Google Sign-In Client (Lazy 초기화)
     */
    val googleSignInClient: GoogleSignInClient by lazy {
        val webClientId = context.getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
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

                if (USE_MOCK) {
                    return@withTimeout signInWithGoogleFirebase(account)
                }

                executeLoginFlow(
                    getIdToken = {
                        account.idToken ?: throw IllegalStateException("Google ID Token을 가져올 수 없습니다")
                    },
                    loginRequest = { idToken ->
                        authApi.googleLogin(GoogleLoginRequest(idToken))
                    },
                    mockLogin = { signInWithGoogleFirebase(account) }
                )
            }
        } catch (e: Exception) {
            handleLoginError("Google", e)
        }
    }

    /**
     * Google Firebase 직접 로그인 (Mock 모드)
     */
    private suspend fun signInWithGoogleFirebase(account: GoogleSignInAccount): AuthResult {
        return try {
            Log.d(TAG, "MOCK 모드: Google Firebase 로그인 시도")

            val idToken = account.idToken
                ?: return AuthResult.Error.Firebase("Google ID Token을 가져올 수 없습니다")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            val user = authResult.user
                ?: return AuthResult.Error.Firebase("Firebase 사용자 정보를 가져올 수 없습니다")

            // Mock JWT 토큰 저장 (실제 환경에서는 사용 금지)
            saveMockTokens("google")

            Log.d(TAG, "Google Mock 로그인 성공: uid=${user.uid}")
            AuthResult.Success(user)

        } catch (e: FirebaseException) {
            Log.e(TAG, "Google Firebase 로그인 실패", e)
            AuthResult.Error.Firebase(e.message ?: "Google 로그인에 실패했습니다")
        } catch (e: Exception) {
            Log.e(TAG, "Google Mock 로그인 실패", e)
            AuthResult.Error.Unknown(e.message ?: "Google 로그인에 실패했습니다", e)
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

                if (USE_MOCK) {
                    return@withTimeout loginWithFirebaseMock()
                }

                // 2. 백엔드로 토큰 전송
                executeLoginFlow(
                    getIdToken = { kakaoToken.accessToken },
                    loginRequest = { accessToken ->
                        authApi.kakaoLogin(KakaoLoginRequest(accessToken))
                    },
                    mockLogin = { loginWithFirebaseMock() }
                )
            }
        } catch (e: Exception) {
            handleLoginError("Kakao", e)
        }
    }

    /**
     * 네이버 로그인 (카카오와 동일한 방법)
     */
    suspend fun loginWithNaver(): AuthResult {
        return try{
            withTimeout(LOGIN_TIMEOUT_MS){
                logLoginAttempt("Naver", null)

                val naverToken = getNaverAccessToken()
                Log.d(TAG, "네이버 토큰 획득 성공")

                if(USE_MOCK){
                    return@withTimeout loginWithFirebaseMock()
                }

                executeLoginFlow(
                    getIdToken = {naverToken.accessToken},
                    loginRequest = {accessToken ->
                        authApi.naverLogin(NaverLoginRequest(accessToken))
                    },
                    mockLogin = { loginWithFirebaseMock() }
                )
            }
        } catch (e: Exception) {
            handleLoginError("Naver", e)
        }
    }

    /**
     * 공통 로그인 플로우 실행
     */
    private suspend fun executeLoginFlow(
        getIdToken: suspend () -> String,
        loginRequest: suspend (String) -> BaseResponse<LoginResponse>,
        mockLogin: suspend () -> AuthResult
    ): AuthResult {
        return try {
            if (USE_MOCK) {
                return mockLogin()
            }

            // ID Token 획득
            val idToken = getIdToken()

            // 백엔드 API 호출
            val response = loginRequest(idToken)

            if (!response.isSuccess || response.result == null) {
                return AuthResult.Error.Api(
                    message = response.message,
                    code = response.code
                )
            }

            // 토큰 저장 및 Firebase 로그인
            saveTokensAndSignInWithFirebase(response.result)

        } catch (e: ApiException) {
            Log.e(TAG, "API 에러", e)
            AuthResult.Error.Api(e.message, e.code)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * JWT 토큰 저장 및 Firebase 로그인
     */
    private suspend fun saveTokensAndSignInWithFirebase(loginData: LoginResponse): AuthResult {
        return try {
            // JWT 토큰 저장
            tokenManager.saveAccessToken(loginData.accessToken)
            tokenManager.saveRefreshToken(loginData.refreshToken)

            // Firebase Custom Token으로 로그인
            val firebaseUser = signInWithFirebaseCustomToken(loginData.firebaseCustomToken)

            AuthResult.Success(firebaseUser)
        } catch (e: Exception) {
            Log.e(TAG, "토큰 저장 또는 Firebase 로그인 실패", e)
            AuthResult.Error.Firebase(e.message ?: "인증 처리 중 오류가 발생했습니다")
        }
    }

    /**
     * Firebase Mock 로그인 (익명 로그인)
     */
    private suspend fun loginWithFirebaseMock(): AuthResult {
        return try {
            Log.d(TAG, "Mock 모드: Firebase 익명 로그인 시도")

            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user
                ?: return AuthResult.Error.Firebase("Firebase 사용자 정보를 가져올 수 없습니다")

            // Mock JWT 토큰 저장
            saveMockTokens("kakao")

            Log.d(TAG, "Mock 로그인 성공: uid=${user.uid}")
            AuthResult.Success(user)

        } catch (e: FirebaseException) {
            Log.e(TAG, "Firebase Mock 로그인 실패", e)
            AuthResult.Error.Firebase(e.message ?: "로그인에 실패했습니다")
        } catch (e: Exception) {
            Log.e(TAG, "Mock 로그인 실패", e)
            AuthResult.Error.Unknown(e.message ?: "로그인에 실패했습니다", e)
        }
    }

    /**
     * 카카오 SDK를 통한 로그인
     */
    private suspend fun kakaoLogin(): OAuthToken = suspendCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    Log.e(TAG, "카카오 로그인 실패: ${error.message}")

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resumeWithException(
                            UserCancellationException("로그인이 취소되었습니다")
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
                        IllegalStateException("카카오 토큰을 받지 못했습니다")
                    )
                }
            }
        }

        // 카카오톡 설치 여부에 따라 로그인 방식 선택
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            Log.d(TAG, "카카오톡으로 로그인 시도")
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    error != null -> {
                        Log.w(TAG, "카카오톡 로그인 실패, 카카오 계정으로 재시도")

                        // 사용자가 취소한 경우 재시도 안 함
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            callback(null, error)
                        } else {
                            // 카카오톡 로그인 실패 시 카카오 계정으로 재시도
                            Log.d(TAG, "카카오 계정으로 로그인 시도")
                            UserApiClient.instance.loginWithKakaoAccount(
                                context,
                                callback = callback
                            )
                        }
                    }

                    token != null -> {
                        callback(token, null)
                    }

                    else -> {
                        callback(null, IllegalStateException("카카오톡 로그인 실패"))
                    }
                }
            }
        } else {
            Log.d(TAG, "카카오 계정으로 로그인 시도")
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * 네이버 Access Token 가져오기
     */
    private fun getNaverAccessToken(): NaverToken {
        val accessToken = NaverIdLoginSDK.getAccessToken()
            ?: throw IllegalStateException("네이버 액세스 토큰을 가져올 수 없습니다.")

        val tokenType = NaverIdLoginSDK.getTokenType() ?: "Bearer"

        return NaverToken(accessToken, tokenType)
    }

    /**
     * Firebase Custom Token으로 로그인
     */
    private suspend fun signInWithFirebaseCustomToken(customToken: String): FirebaseUser {
        Log.d(TAG, "Firebase Custom Token 로그인 시도")
        val authResult = firebaseAuth.signInWithCustomToken(customToken).await()
        val user = authResult.user
            ?: throw IllegalStateException("Firebase 사용자 정보를 가져올 수 없습니다")
        Log.d(TAG, "Firebase 로그인 성공: uid=${user.uid}")
        return user
    }

    /**
     * Mock 토큰 저장 (개발용)
     * 주의: 프로덕션 환경에서는 절대 사용 금지
     */
    private fun saveMockTokens(provider: String) {
        if (!BuildConfig.DEBUG) {
            Log.w(TAG, "프로덕션 환경에서 Mock 토큰 저장 시도 - 무시됨")
            return
        }

        val timestamp = System.currentTimeMillis()
        tokenManager.saveAccessToken("mock_${provider}_access_$timestamp")
        tokenManager.saveRefreshToken("mock_${provider}_refresh_$timestamp")
    }

    /**
     * 현재 로그인된 Firebase 사용자 반환
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn() && getCurrentUser() != null
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
                googleSignInClient.signOut().await()
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

            // 4. 토큰 정리 및 Firebase 로그아웃 (항상 실행)
            tokenManager.clearTokens()
            firebaseAuth.signOut()

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
                firebaseAuth.signOut()
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

            // 2. Firebase 계정 삭제
            runCatching {
                getCurrentUser()?.delete()?.await()
                Log.d(TAG, "Firebase 계정 삭제 성공")
            }.onFailure {
                Log.e(TAG, "Firebase 계정 삭제 실패", it)
                errors.add(it)
            }

            // 3. Google 로그아웃 (연결 끊기는 별도 API 필요)
            runCatching {
                googleSignInClient.signOut().await()
                Log.d(TAG, "Google 로그아웃 성공")
            }.onFailure {
                Log.e(TAG, "Google 로그아웃 실패", it)
                errors.add(it)
            }

            // 4. 네이버 연결 끊기
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

            // 5. 토큰 정리 (항상 실행)
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
                firebaseAuth.signOut()
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
            is FirebaseException -> {
                AuthResult.Error.Firebase(error.message ?: "$provider 인증 실패")
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
        // GoogleSignInClient는 lazy로 초기화되므로
        // 명시적 정리가 필요한 경우 여기서 처리
        Log.d(TAG, "AuthRepository 리소스 정리")
    }
}

/**
 * 사용자 취소를 나타내는 Exception
 */
private class UserCancellationException(message: String) : Exception(message)