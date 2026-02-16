package com.example.momenty.global.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 토큰 관리
 * EncryptedSharedPreferences를 사용하여 토큰을 암호화하여 저장
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // EncryptedSharedPreferences 초기화 실패 시 일반 SharedPreferences 사용
            // (API 23 미만 또는 특정 기기에서 발생 가능)
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val PREF_NAME = "momenty_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginInfo(
        accessToken: String,
        refreshToken: String,
        userId: Long) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Mock 모드용 로그인 정보 저장
     * 개발 및 테스트 시 사용
     */
    fun saveMockLoginInfo() {
        saveLoginInfo(
            accessToken = "mock_access_token_dev",
            refreshToken = "mock_refresh_token_dev",
            userId = 1L
        )
    }

    /**
     * Token 저장
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String) {
        prefs.edit().apply{
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Access Token 조회
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Refresh Token 조회
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * User ID 조회
     * Mock 모드에서는 저장된 userId가 없을 경우 테스트용 ID(1L) 반환
     * @return userId (없으면 -1L 반환, Mock 모드에서는 1L 반환)
     */
    fun getUserId(): Long {
        val savedUserId = prefs.getLong(KEY_USER_ID, -1L)

        // 저장된 userId가 있으면 그것을 반환
        if (savedUserId != -1L) {
            return savedUserId
        }

        // Mock 모드 체크: accessToken이 "mock_"으로 시작하면 Mock 모드로 간주
        val accessToken = getAccessToken()
        if (accessToken != null && accessToken.startsWith("mock_")) {
            // Mock 모드에서는 테스트용 userId 반환
            return 1L
        }

        return -1L
    }

    /**
     * 모든 토큰 삭제 (로그아웃 시)
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    /**
     * 로그인 여부 확인
     * Mock 모드에서는 mock_ 토큰이 있으면 로그인된 것으로 간주
     */
    fun isLoggedIn(): Boolean {
        val token = getAccessToken()

        // Mock 모드 체크
        if (token != null && token.startsWith("mock_")) {
            return true
        }

        return !token.isNullOrEmpty() && prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * 토큰 존재 여부 확인
     */
    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }
}