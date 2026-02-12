package com.example.momenty.data.repository

/**
 * 인증 결과를 나타내는 Sealed Class
 * Firebase 제거 - 백엔드 JWT만 사용
 */
sealed class AuthResult {
    /**
     * 로그인 성공
     * @param userName 사용자 이름 (소셜 로그인에서 가져온 정보)
     * @param email 이메일
     */
    data class Success(
        val userName: String?,
        val email: String?
    ) : AuthResult()

    sealed class Error(open val message: String) : AuthResult() {
        data class Network(override val message: String) : Error(message)
        data class Api(override val message: String, val code: String) : Error(message)
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
 * 사용자 취소를 나타내는 Exception
 */
class UserCancellationException(message: String) : Exception(message)