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
    }

    /**
     * Access Token 저장
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
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
     * User ID 저장
     */
    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    /**
     * User ID 조회
     * @return userId (없으면 -1L 반환)
     */
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    /**
     * User ID 존재 여부 확인
     */
    fun hasUserId(): Boolean {
        return getUserId() != -1L
    }

    /**
     * 모든 토큰 삭제 (로그아웃 시)
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            apply()
        }
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        val token = getAccessToken()
        val userId = getUserId()
        return !token.isNullOrEmpty() && userId != -1L
    }

    /**
     * 두 토큰을 한 번에 저장 (로그인 성공 시)
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    /**
     * 로그인 정보 일괄 저장 (토큰 + userId)
     */
    fun saveLoginInfo(accessToken: String, refreshToken: String, userId: Long) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_USER_ID, userId)
            apply()
        }
    }

    /**
     * 토큰 존재 여부 확인
     */
    fun hasValidToken(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }
}