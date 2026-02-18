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
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Mock 모드용 로그인 정보 저장
     * 개발 및 테스트 시 사용
     */
    fun saveMockLoginInfo() {
        saveTokens(
            accessToken = "mock_access_token_dev",
            refreshToken = "mock_refresh_token_dev"
        )
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
     * 모든 토큰 삭제 (로그아웃 시)
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
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